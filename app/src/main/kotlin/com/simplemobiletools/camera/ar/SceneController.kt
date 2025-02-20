package com.simplemobiletools.camera.ar

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.view.isGone
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
import io.github.sceneview.ar.node.ARCameraNode
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.scene.PlaneRenderer
import io.github.sceneview.math.Direction
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ImageNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.utils.readBuffer

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
	private val conditionHandlers = sceneLoader.conditionHandlers
	val assetHandlers = sceneLoader.assetHandlers

	private val projectAssets = sceneView.context.assets

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
		//TODO: Fetch remote
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

	fun getParentNode(anchor: Anchor): Node? = sceneState.getParentNode(anchor)
	fun hasParentNode(visualAsset: VisualAsset): Boolean = sceneState.hasParentNode(visualAsset)
	fun getParentNode(visualAsset: VisualAsset): Node? = sceneState.getParentNode(visualAsset)
	fun setParentNode(trackable: Trackable, node: Node) = sceneState.setParentNode(trackable, node)
	fun getFeature(visualAsset: VisualAsset): Feature = sceneState.getFeature(visualAsset)
	fun getAnchor(visualAsset: VisualAsset): Anchor = sceneState.getAnchor(visualAsset)

	//== Asset stuff: ==/

	fun attachModel(node: Node, model: Model) {
		//TODO: Preload models
		sceneView.modelLoader.loadModelInstanceAsync(model.href) { modelInstance ->
			if (modelInstance == null) {
				Log.e(TAG, "Failed to load model instance: ${model.href}.")
				return@loadModelInstanceAsync
			}

			// From -180->180 to 0->360
			val x = if (model.rotationVector.x < 0f) 360f + model.rotationVector.x else model.rotationVector.x
			val y = if (model.rotationVector.y < 0f) 360f + model.rotationVector.y else model.rotationVector.y
			val z = if (model.rotationVector.z < 0f) 360f + model.rotationVector.z else model.rotationVector.z
			val rotation = Rotation(x, y, z)

			val modelNode = ModelNode(
				modelInstance = modelInstance,
				//FIXME: Doesn't appear to be doing anything. Probably overwritten by transform scale
				//scaleToUnits = 1f,
				//FIXME: Feel free to change this
				// (I have a feeling that it doesn't change anything)
				// (also, it's better for some models to be on the base, but for some, like the test axis, it should keep the original center)
				centerOrigin = null //Position(0f, -1f, 0f),
			).apply {
				isEditable = true

				transform(
					position = Position(0f, 0f, 0f),
					//FIXME: Rotation order: Z, Y, X. Want X-Y-Z, I think...
					rotation = rotation,
					scale = model.scaleVector
				)
			}

			sceneState.addVisualAssetToScene(
				node = node,
				visualAssetNode = modelNode,
				visualAsset = model,
				show = model.evaluateConditions()
			)

			modelNode.onSingleTapUp = { selectionModule.toggleSelected(model); true }
		}
	}

	fun attachImage(node: Node, image: Image) {
		//TODO: Preload images
		//TODO: Fetch remote image
		val bitmap: Bitmap = BitmapFactory.decodeStream(projectAssets.open(image.href))

		var imageWidth = 1f
		var imageHeight = 1f

		if (image.width is SizePercentage || image.height is SizePercentage) {
			//TODO: percentages
			//Keep default
		} else if (image.width == null && image.height == null) {
			//TODO: get extentX and extentY from plane (PlaneTrackingModule). What about image tracking?
			//Keep default
		}
		else if (image.width == null) {
			imageHeight = (image.height!! as SizeAbsolute).m
			imageWidth = imageHeight * (bitmap.width.toFloat() / bitmap.height.toFloat())
		}
		else if (image.height == null) {
			imageWidth = (image.width!! as SizeAbsolute).m
			imageHeight = imageWidth * (bitmap.height.toFloat() / bitmap.width.toFloat())
		}
		else {
			imageWidth = (image.width!! as SizeAbsolute).m
			imageHeight = (image.height!! as SizeAbsolute).m
		}

		val size = io.github.sceneview.math.Size(imageWidth, imageHeight)

		// From -180->180 to 0->360
		val x = if (image.rotationVector.x < 0f) 360f + image.rotationVector.x else image.rotationVector.x
		val y = if (image.rotationVector.y < 0f) 360f + image.rotationVector.y else image.rotationVector.y
		val z = if (image.rotationVector.z < 0f) 360f + image.rotationVector.z else image.rotationVector.z
		val rotation = Rotation(x, y, z)

		val imageNode = ImageNode(
			materialLoader = sceneView.materialLoader,
			bitmap = bitmap,
			size = size,
			normal = Direction(0f),
			//center = Position(0f, 0f, 0f)
		).apply {
			transform(
				position = Position(0f, 0f, 0f),
				//FIXME: Rotation order: Z, Y, X. Want X-Y-Z, I think...
				rotation = rotation,
				scale = io.github.sceneview.math.Size(1f, 1f, 1f)
			)
		}

		sceneState.addVisualAssetToScene(
			node = node,
			visualAssetNode = imageNode,
			visualAsset = image,
			show = image.evaluateConditions()
		)

		imageNode.onSingleTapUp = { selectionModule.toggleSelected(image); true }
	}


	//== RelativeTo Stuff: ==/

	fun addRelativeNodeToUser(relativeTo: RelativeTo) {
		val newNode = sceneState.addRelativeNodeToUser(relativeTo)

		relativeTo.assets.forEach {
			assetHandlers.getOrElse(it.arElementType) {
				Log.w(TAG, "Got a ${it.arElementType} asset. That type is not supported yet. Ignoring...")
				null
			}?.invoke(newNode, it)
		}
	}

	fun addRelativeNode(relativeTo: RelativeTo, other: Trackable) {
		sceneState.getParentNode(other)?.let { this.addRelativeNode(relativeTo, it) }
	}

	fun addRelativeNode(relativeTo: RelativeTo, other: RelativeTo) {
		sceneState.getParentNode(other)?.let { this.addRelativeNode(relativeTo, it) }
	}

	fun addRelativeNode(relativeTo: RelativeTo, other: Node) {
		val newNode = when (other) {
			is AnchorNode -> sceneState.addRelativeAnchorNode(relativeTo, other)
			is ARCameraNode -> sceneState.addRelativeNodeToUser(relativeTo) //Should not get here
			else -> sceneState.addRelativeNode(relativeTo, other)
		}

		relativeTo.assets.forEach {
			assetHandlers.getOrElse(it.arElementType) {
				Log.w(TAG, "Got a ${it.arElementType} asset. That type is not supported yet. Ignoring...")
				null
			}?.invoke(newNode, it)
		}
	}

	fun propagateNode(original: Anchor) {
		if (sceneState.isAwaited(original)) {
			val originalNode = sceneState.getParentNode(original)!!

			sceneState.getWaitingFor(original)!!.forEach { new ->
				addRelativeNode(new, originalNode)
				Log.d(TAG, "Assigned node to RelativeTo(id=${new.id})")

				// Call recursively for RelativeTo relative to RelativeTo :)
				propagateNode(new)
			}

			sceneState.clearQueuedRelativeAnchors(original)
		}
	}
}
