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
import io.github.sceneview.ar.scene.PlaneRenderer
import io.github.sceneview.utils.readBuffer
import kotlinx.coroutines.launch

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

	private val sceneState: SceneState = SceneState(sceneView)
	private val sceneLoader : SceneLoader = SceneLoader(this, sceneState)
	val trackerHandler = sceneLoader.trackerHandler
	val arElementHandlers = sceneLoader.arElementHandlers
	val anchorHandlers = sceneLoader.anchorHandlers
	val relativeToRefHandler = sceneLoader.relativeToRefHandler
	val conditionHandlers = sceneLoader.conditionHandlers
	val assetHandlers = sceneLoader.assetHandlers

	// Keep track of functions to execute every frame (e.g. checking conditions); organize by ID
	// DON'T ITERATE DIRECTLY, use copy on every loop. Locks are complicated to use here. Can change at runtime at will
	private val thingsToDo : HashMap<String, ((Session, Frame) -> Unit)> = HashMap()

	private var execute: Boolean = false
	fun run() {
		execute = true
		Log.d(TAG, "Running scene...")
	}
	fun stop() {
		execute = false
		Log.d(TAG, "Stopping scene...")
	}

	private var updateSceneRequested : Boolean = false
	fun requestSceneUpdate() {
		updateSceneRequested = true
		Log.d(TAG, "Scene update requested")
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
			context.binding.instructionText.text = value
		}

	init {
		sceneView.apply {
			planeRenderer.apply {
				planeRenderer.isEnabled = true
				planeRenderer.planeRendererMode = PlaneRenderer.PlaneRendererMode.RENDER_CENTER
			}
			configureSession { session, config ->
				config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
					true -> Config.DepthMode.AUTOMATIC
					else -> Config.DepthMode.DISABLED
				}
				config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
				config.instantPlacementMode = Config.InstantPlacementMode.DISABLED

				Log.d(TAG, "Creating modules...")
				selectionModule = SelectionModule(this@SceneController, sceneView)
				distanceModule = DistanceModule(this@SceneController, sceneView)
				planeTrackingModule = PlaneTrackingModule(this@SceneController, sceneView)
				imageTrackingModule = ImageTrackingModule(this@SceneController, sceneView, session)
				Log.d(TAG, "Creating modules... DONE")
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

				Log.d(TAG, "Clearing scene... DONE")

				// Load new one
				if (queuedARML != null) {
					arml = queuedARML!!
					queuedARML = null
				}
				sceneLoader.load(arml)

				isLoading = false
				run()
			}
		}
	}



	//=== ARML ===//

	// DON'T CHANGE THIS DIRECTLY. Set queuedARML, and the program will figure it out
	private var arml : ARML = ARML()
		set(value) {
			field = value
			Log.d(TAG, "Set ARML to $value")
		}

	private var queuedARML : ARML? = null
		set(value) {
			if (value != null) {
				Log.d(TAG, "ARML change requested: $value")

				Log.d(TAG, "Validating...")
				val validation = value.validate()
				if (!validation.first) {
					Log.e(TAG, "Invalid ARML: ${validation.second}")
					instructionText = "Invalid ARML!"
				} else {
					Log.d(TAG, "Validating... OK")
					field = value
					requestSceneUpdate()
				}
			} else {
				field = null
			}
		}

	fun setARML(arml: ARML) {
		queuedARML = arml
	}

	fun setARMLFromPath(armlPath: String) {
		Log.d(TAG, "Loading ARML from path: $armlPath")

		// Read content
		val armlContent = try {
			Log.d(TAG, "Reading content...")
			context.assets.open(armlPath).readBuffer().array().decodeToString()
		} catch (e : Exception) {
			Log.e(TAG, "Failed to read ARML file.", e)
			instructionText = "Failed to read ARML file!"
			throw e
		}

		// Parse ARML
		try {
			Log.d(TAG, "Parsing...")
			val result : ARML = ARMLParser().loads(armlContent)
			Log.d(TAG, "Parsing... OK")
			queuedARML = result
		} catch (e : Exception) {
			Log.e(TAG, "Failed to parse ARML.", e)
			instructionText = "Failed to parse ARML!"
			throw e
		}
	}






	//=== Module Stuff ===/

	private fun VisualAsset.evaluateConditions(): Boolean {
		this.conditions.forEach {
			val result = conditionHandlers.getOrElse(it.arElementType) {
				Log.w(TAG, "Got a ${it.arElementType} condition. That type has not been implemented yet. Ignoring...")
				null
			}?.invoke(this, it) ?: true

			if (!result) return false
		}
		return true
	}

	fun handleImageTracker(trackable: Trackable, config: TrackableConfig) {
		if (!imageTrackingModule.isEnabled()) {
			imageTrackingModule.enable()
			thingsToDo["ImageTracking"] = { _, frame -> imageTrackingModule.onFrameUpdate(this, frame) }
		}

		//TODO: Fetch remote
		val bitmap = BitmapFactory.decodeStream(context.assets.open(config.src))
		imageTrackingModule.addImageToDatabase(config.src, bitmap, trackable.size)
		imageTrackingModule.addToImageQueue(trackable, config)
	}

	fun handlePlaneTracker(trackable: Trackable, planeType: Plane.Type) {
		if (!planeTrackingModule.isEnabled()) {
			planeTrackingModule.enable()
			thingsToDo["PlaneTracking"] = { _, frame -> planeTrackingModule.onFrameUpdate(this, frame) }
		}

		planeTrackingModule.addToPlaneQueue(trackable, planeType)
	}




	// === Scene State Interface === //

	fun addToScene(trackable: Trackable, anchor: com.google.ar.core.Anchor) = sceneState.addToScene(trackable, anchor)
	fun getAnchorNode(anchor: Anchor): AnchorNode? = sceneState.getAnchorNode(anchor)
	fun hasAnchorNode(visualAsset: VisualAsset): Boolean = sceneState.hasAnchorNode(visualAsset)
	fun getAnchorNode(visualAsset: VisualAsset): AnchorNode? = sceneState.getAnchorNode(visualAsset)

	//== Asset stuff: ==/

	fun attachModel(anchorNode: AnchorNode, model: Model) {
		//TODO: Preload models
		context.lifecycleScope.launch {
			val modelNode = sceneState.attachModel(anchorNode, model, show = model.evaluateConditions())

			selectionModule.setSelected(model, false)
			modelNode?.onDoubleTap = { selectionModule.toggleSelected(model); true }  //FIXME: Only works for some?
		}
	}

	fun attachImage(anchorNode: AnchorNode, image: Image) {
		//TODO: Preload images
		//context.lifecycleScope.launch {
			val imageNode = sceneState.attachImage(anchorNode, image, show = image.evaluateConditions())

			selectionModule.setSelected(image, false)
			imageNode?.onDoubleTap = { selectionModule.toggleSelected(image); true }
		//}
	}

	//== RelativeTo Stuff: ==/

	//TODO
	fun addRelativeAnchorNodeToUser(relativeTo: RelativeTo) {
		val newNode = sceneState.addRelativeAnchorNodeToUser(relativeTo)

		/*
		relativeTo.assets.forEach {
			assetHandlers.getOrElse(it.arElementType) {
				Log.w(TAG, "Got a ${it.arElementType} asset. That type is not supported yet. Ignoring...")
				null
			}?.invoke(newNode, it)
		}
		 */
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
				Log.w(TAG, "Got a ${it.arElementType} asset. That type is not supported yet. Ignoring...")
				null
			}?.invoke(newAnchorNode, it)
		}
	}

	fun propagateAnchorNode(original: Anchor) {
		if (sceneState.isAwaited(original)) {
			val originalAnchorNode = sceneState.getAnchorNode(original)!!

			sceneState.getWaitingFor(original)!!.forEach { new ->
				addRelativeAnchorNode(new, originalAnchorNode)
				Log.d(TAG, "Assigned anchor to RelativeTo(id=${new.id})")

				// Call recursively for RelativeTo relative to RelativeTo :)
				propagateAnchorNode(new)
			}

			sceneState.clearQueuedRelativeAnchors(original)
		}
	}
}
