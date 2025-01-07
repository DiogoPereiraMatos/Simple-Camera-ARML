package com.simplemobiletools.camera.ar

import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.simplemobiletools.camera.activities.SceneviewActivity
import com.simplemobiletools.camera.ar.arml.ARMLParser
import com.simplemobiletools.camera.ar.arml.elements.*
import com.simplemobiletools.camera.ar.modules.DistanceModule
import com.simplemobiletools.camera.ar.modules.ImageTrackingModule
import com.simplemobiletools.camera.ar.modules.PlaneTrackingModule
import com.simplemobiletools.camera.ar.modules.SelectionModule
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.utils.readBuffer
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantReadWriteLock

class SceneController(
	private val context: SceneviewActivity,
	private val sceneView: ARSceneView,
) {

	companion object {
		private const val TAG = "SCENE_CONTROLLER"
	}

	// Initialized in session config
	// not strictly required, but some might need access to the session (e.g. imageTracking)
	lateinit var selectionModule: SelectionModule
	lateinit var distanceModule: DistanceModule
	private lateinit var planeTrackingModule: PlaneTrackingModule
	private lateinit var imageTrackingModule: ImageTrackingModule

	private val sceneState: SceneState = SceneState(context, sceneView)
	private val sceneLoader : SceneLoader = SceneLoader(this, sceneState)
	val trackerHandler = sceneLoader.trackerHandler
	val arElementHandlers = sceneLoader.arElementHandlers
	val anchorHandlers = sceneLoader.anchorHandlers
	val relativeToRefHandler = sceneLoader.relativeToRefHandler
	val conditionHandlers = sceneLoader.conditionHandlers
	val assetHandlers = sceneLoader.assetHandlers


	// Keep track of functions to execute every frame (e.g. checking conditions); organize by ID
	private val thingsToDo : HashMap<String, ((Session, Frame) -> Unit)> = HashMap()

	private val stateLock: ReentrantReadWriteLock = ReentrantReadWriteLock()

	private var execute: Boolean = false
	fun run() { execute = true }
	fun stop() { execute = false }

	private var updateSceneRequested : Boolean = false
	fun requestSceneUpdate() {
		updateSceneRequested = true
	}

	private var isLoading = false
		set(value) {
			field = value
			context.binding.loadingView.isGone = !value
			instructionText = if (value) "Loading..." else ""
		}

	private var instructionText: String = ""
		set(value) {
			field = value
			context.binding.instructionText.text = instructionText
		}

	init {
		sceneView.apply {
			configureSession { session, config ->
				config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
					true -> Config.DepthMode.AUTOMATIC
					else -> Config.DepthMode.DISABLED
				}
				config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
				config.instantPlacementMode = Config.InstantPlacementMode.DISABLED

				selectionModule = SelectionModule(sceneView, sceneState)
				distanceModule = DistanceModule(sceneView, sceneState)
				planeTrackingModule = PlaneTrackingModule(sceneView, sceneState)
				imageTrackingModule = ImageTrackingModule(sceneView, sceneState, session)
			}
			onSessionUpdated = { session, frame ->
				if (execute) {
					val thingsToDoNow = ArrayList(thingsToDo.values)  // clone
					thingsToDoNow.forEach { it(session, frame) }
				}
			}
			onTrackingFailureChanged = { reason ->
				this@SceneController.instructionText = reason?.getDescription(context) ?: instructionText
			}
		}

		// OnFrameUpdate: Check conditions and set visibility accordingly
		thingsToDo["CheckConditions"] = { _, _ ->
			sceneState.conditionalVisualAssets.forEach {
				sceneState.setVisibility(it, it.evaluateConditions())
			}
		}

		// OnFrameUpdate: Refresh scene when requested
		// Only add this at the end. clearScene clears thingsToDo list, so there will be a problem with concurrency
		thingsToDo["RefreshScene"] = { _, _ ->
			if (updateSceneRequested) {
				stop()
				updateSceneRequested = false
				isLoading = true

				// Clear previous scene
				Log.d(TAG, "Clearing scene...")
				sceneState.reset()

				thingsToDo.remove("PlaneTracking")
				planeTrackingModule.disable()
				planeTrackingModule.reset()

				thingsToDo.remove("ImageTracking")
				imageTrackingModule.disable()
				imageTrackingModule.reset()

				Log.d(TAG, "Clearing scene... DONE!")

				// Load new one
				sceneLoader.load(arml)

				isLoading = false
				run()
			}
		}
	}



	//=== ARML ===//
	private var arml : ARML = ARML()
		get() {
			stateLock.readLock().lock()
			val result = field
			stateLock.readLock().unlock()
			return result
		}
		set(value) {
			stateLock.writeLock().lock()
			field = value
			Log.d(TAG, "Set arml to $value")
			requestSceneUpdate()
			stateLock.writeLock().unlock()
		}

	fun setARMLFromPath(armlPath: String) {
		Log.d(TAG, "Set armlPath to $armlPath")

		// Read content
		val armlContent = try {
			context.assets.open(armlPath).readBuffer().array().decodeToString()
		} catch (e : Exception) {
			Log.e(TAG, "Failed to read ARML file.", e)
			instructionText = "Failed to read ARML file!"
			throw e
		}

		// Parse ARML
		try {
			val result : ARML = ARMLParser().loads(armlContent)

			val validation = result.validate()
			if (!validation.first) {
				Log.e(TAG, "Invalid ARML: ${validation.second}")
				instructionText = "Invalid ARML!"
			} else {
				arml = result
			}
		} catch (e : Exception) {
			Log.e(TAG, "Failed to parse ARML.", e)
			instructionText = "Failed to parse ARML!"
			throw e
		}
	}







	private fun VisualAsset.evaluateConditions(): Boolean {
		this.conditions.forEach {
			val result = conditionHandlers.getOrElse(it.arElementType) {
				Log.w(TAG, "Got a ${it.arElementType} condition. That type has not been implemented yet.")
				null
			}?.invoke(this, it) ?: true

			if (!result) return false
		}
		return true
	}

	fun handleImageTracker(trackable: Trackable, config: TrackableConfig) {
		if (!imageTrackingModule.isEnabled()) {
			imageTrackingModule.enable()
			//loopLock.writeLock().lock()
			thingsToDo["ImageTracking"] = { _, frame -> imageTrackingModule.onFrameUpdate(this, frame) }
			//loopLock.writeLock().unlock()
		}

		//TODO: Fetch remote
		val bitmap = BitmapFactory.decodeStream(context.assets.open(config.src))
		imageTrackingModule.addImageToDatabase(config.src, bitmap)
		imageTrackingModule.addToImageQueue(trackable, config)
	}

	fun handlePlaneTracker(trackable: Trackable, planeType: Plane.Type) {
		if (!planeTrackingModule.isEnabled()) {
			planeTrackingModule.enable()
			//loopLock.writeLock().lock()
			thingsToDo["PlaneTracking"] = { _, frame -> planeTrackingModule.onFrameUpdate(this, frame) }
			//loopLock.writeLock().unlock()
		}

		planeTrackingModule.addToPlaneQueue(trackable, planeType)
	}

	fun attachModel(anchorNode: AnchorNode, model: Model) {
		context.lifecycleScope.launch {
			val modelNode = sceneState.attachModel(anchorNode, model, show = model.evaluateConditions())

			//FIXME: Not working
			selectionModule.setSelected(model, false)
			modelNode?.onDoubleTap = { selectionModule.toggleSelected(model); true }
		}
	}

	fun attachImage(anchorNode: AnchorNode, image: Image) {
		//context.lifecycleScope.launch {
			val imageNode = sceneState.attachImage(anchorNode, image, show = image.evaluateConditions())

			//FIXME: Not working
			selectionModule.setSelected(image, false)
			imageNode?.onDoubleTap = { selectionModule.toggleSelected(image); true }
		//}
	}


	fun addRelativeAnchorNode(relativeTo: RelativeTo, other: Trackable) {
		sceneState.getAnchorNode(other)?.let { this.addRelativeAnchorNode(relativeTo, it) }
	}

	fun addRelativeAnchorNode(relativeTo: RelativeTo, other: RelativeTo) {
		sceneState.getAnchorNode(other)?.let { this.addRelativeAnchorNode(relativeTo, it) }
	}

	fun addRelativeAnchorNode(relativeTo: RelativeTo, other: AnchorNode) {
		val newAnchorNode = sceneState.addRelativeAnchorNode(relativeTo, other)

		relativeTo.assets.forEach {
			assetHandlers.getOrElse(it.arElementType) {
				Log.w(TAG, "Got a ${it.arElementType} asset. That type is not supported yet.")
				null
			}?.invoke(newAnchorNode, it)
		}
	}

	fun copyAnchorNode(original: Anchor) {
		if (sceneState.isAwaited(original)) {
			val originalAnchorNode = sceneState.getAnchorNode(original)!!

			sceneState.getWaitingFor(original)!!.forEach { new ->
				addRelativeAnchorNode(new, originalAnchorNode)
				Log.d(TAG, "Assigned anchor to $new")

				// Call recursively
				copyAnchorNode(new)
			}

			sceneState.clearQueuedRelativeAnchors(original)
		}
	}
}
