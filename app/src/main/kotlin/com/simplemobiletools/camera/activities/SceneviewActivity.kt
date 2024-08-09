package com.simplemobiletools.camera.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.*
import com.google.ar.core.Anchor
import com.simplemobiletools.camera.R
import com.simplemobiletools.camera.ar.arml.ARMLParser
import com.simplemobiletools.camera.ar.arml.elements.*
import com.simplemobiletools.camera.ar.arml.elements.Trackable
import com.simplemobiletools.camera.ar.arml.elements.gml.Point
import com.simplemobiletools.camera.databinding.ActivitySceneviewBinding
import com.simplemobiletools.camera.extensions.config
import com.simplemobiletools.commons.extensions.viewBinding
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.arcore.distanceTo
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.utils.readBuffer
import kotlinx.coroutines.launch


class SceneviewActivity : SimpleActivity() {

	companion object val TAG = "ARML"

	private val binding by viewBinding(ActivitySceneviewBinding::inflate)

	var myModelPath : String = "models/damaged_helmet.glb"
		set(value) {
			if (field != value) {
				Log.d(TAG, "Set virtualObjectName to $value")
				updateSceneRequested = true
			}
			field = value
		}
	var armlPath : String = "armlexamples/empty.xml"
		set(value) {
			if (field != value) {
				Log.d(TAG, "Set armlPath to $value")
				updateSceneRequested = true
			}
			field = value
		}
	var updateSceneRequested : Boolean = false

	var isLoading = false
		set(value) {
			field = value
			binding.loadingView.isGone = !value
		}

	var trackingFailureReason: TrackingFailureReason? = null
		set(value) {
			if (field != value) {
				field = value
				updateInstructions()
			}
		}

	fun updateInstructions() {
		binding.instructionText.text = trackingFailureReason?.let { it.getDescription(this) }
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		useDynamicTheme = false
		super.onCreate(savedInstanceState)
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		supportActionBar?.hide()

		setContentView(binding.root)

		window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
		//window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

		WindowCompat.setDecorFitsSystemWindows(window, false)
		ViewCompat.setOnApplyWindowInsetsListener(binding.rootView) { _, windowInsets ->
			val safeInsetTop = windowInsets.displayCutout?.safeInsetTop ?: 0

			binding.settings.updateLayoutParams<ViewGroup.MarginLayoutParams> {
				topMargin = safeInsetTop
			}

			binding.arSettingsButton.updateLayoutParams<ViewGroup.MarginLayoutParams> {
				topMargin = safeInsetTop
			}

			//val safeInsetBottom = windowInsets.displayCutout?.safeInsetBottom ?: 0
			//val marginBottom = safeInsetBottom + navigationBarHeight + resources.getDimensionPixelSize(com.simplemobiletools.commons.R.dimen.bigger_margin)

			WindowInsetsCompat.CONSUMED
		}

		val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
		windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
		windowInsetsController.hide(WindowInsetsCompat.Type.statusBars() /*or WindowInsetsCompat.Type.navigationBars()*/)

		binding.apply {
			setupSceneView()
			setupButtons()
		}

		// ARML
		isLoading = true
		handleARML()
		isLoading = false
	}

	override fun onResume() {
		super.onResume()
		if (!this.config.isArmlEnabled)
			this.finish()

		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		ensureTransparentNavigationBar()
	}

	override fun onPause() {
		super.onPause()
		window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
	}

	private fun ensureTransparentNavigationBar() {
		window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)
	}

	private fun setupSceneView() {
		val sceneView = binding.sceneView
		sceneView.apply {
			planeRenderer.isEnabled = false
			configureSession { session, config ->
				config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
					true -> Config.DepthMode.AUTOMATIC
					else -> Config.DepthMode.DISABLED
				}
				config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
				config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
			}
			onSessionUpdated = { session, frame -> thingsToDo.forEach {it(session, frame)} }
			onTrackingFailureChanged = { reason ->
				this@SceneviewActivity.trackingFailureReason = reason
			}
		}
	}

	private fun setupButtons() {
		binding.settings.setOnClickListener {
			launchSettings()
		}
		binding.arSettingsButton.setOnClickListener { v ->
			PopupMenu(this, v).apply {
				setOnMenuItemClickListener { item ->
					when (item.itemId) {
						R.id.refresh_scene -> updateSceneRequested=true
						R.id.model_settings -> launchModelSettingsMenuDialog()
						R.id.arml_settings -> launchARMLSettingsMenuDialog()
						else -> null
					} != null
				}
				inflate(R.menu.sceneview_settings_menu)
				show()
			}
		}
	}

	private fun listAssets(path: String): ArrayList<String>? {

		val fileOrFolder = assets.list(path)!!
		if (fileOrFolder.isEmpty())
			return null

		val allAssets = ArrayList<String>()
		for (f in fileOrFolder) {
			val recursive = listAssets("$path/$f")
			if (recursive != null) {
				// is folder
				allAssets.addAll(recursive)
			} else {
				// is file
				allAssets.add("$path/$f")
			}
		}
		return allAssets
	}

	private fun launchModelSettingsMenuDialog() {
		val models = listAssets("models")!!.filter { filename ->
			filename.endsWith(".glb", true)
				||
				filename.endsWith(".gltf", true)
		}.toTypedArray()

		AlertDialog.Builder(this)
			.setTitle("Model")
			.setSingleChoiceItems(models, models.indexOf(myModelPath)) { _, which -> myModelPath = models[which] }
			.show()
	}

	private fun launchARMLSettingsMenuDialog() {
		val strings = listAssets("armlexamples")!!
			.toTypedArray()

		AlertDialog.Builder(this)
			.setTitle("ARML")
			.setSingleChoiceItems(strings, strings.indexOf(armlPath)) { _, which -> armlPath = strings[which] }
			.show()
	}

	fun launchSettings() {
		val intent = Intent(this, SettingsActivity::class.java)
		startActivity(intent)
	}


	//=== ARML ===//
	// Needs to be var and be initialized as empty, or app crashes :)
	// (Assets not initialized yet)
	var armlContent : String = ARMLParser.EMPTY
		get() = assets.open(armlPath).readBuffer().array().decodeToString()
			.replace("\$myModelPath", myModelPath)

	val arml : ARML
		get() {
			try {
				val result : ARML = ARMLParser().loads(armlContent)

				val validation = result.validate()
				if (!validation.first) {
					Log.e(TAG, "Invalid ARML: ${validation.second}")
					return ARML()
				}
				return result
			} catch (e : Exception) {
				Log.e(TAG, "Failed to parse ARML.", e)
				return ARML()
			}
		}

	private val thingsToDo : ArrayList<((Session, Frame) -> Unit)> = ArrayList()

	// Trackables go in here vvv
	private val queuedAnchors : ArrayList<Trackable> = ArrayList()

	// RelativeTo waiting for trackables go in here vvv
	private val queuedRelativeAnchors : HashMap<com.simplemobiletools.camera.ar.arml.elements.Anchor, ArrayList<RelativeTo>> = HashMap()

	// Assigned Trackables and RelativeTo go in here vvv
 	private val assignedAnchors : HashMap<com.simplemobiletools.camera.ar.arml.elements.Anchor, AnchorNode> = HashMap()

	// Save anchor of placed assets for the conditions
	private val placedAssets : HashMap<VisualAsset, AnchorNode> = HashMap()

	// Save modelNodes of placed models tho be created or destroyed in accordance to the conditions
	private val modelNodes : HashMap<VisualAsset, ModelNode> = HashMap()

	// Keep track of what assets are selected for the conditions
	private val isSelected : HashMap<VisualAsset, Boolean> = HashMap()

	// Assets with Conditions go here, where they are checked every frame and hidden/shown accordingly vvv
	private val conditionalAssets : HashMap<VisualAsset, List<Condition>> = HashMap()

	/*
						+----------+
						|  Scene   |
						+----------+
						     |
		   -------------------------------------------------------------   ...
		   |                                      |
		+---------------+                 +---------------+
		| Trackable     |                 | Trackable     |
		| (Anchor Node) |                 | (Anchor Node) |
		+---------------+                 +---------------+
		   |
		 --------------------------------------------------------------   ...
		 |                    |                        |
		+-------------+   +-------------+    +--------------+
		| Model       |   | Model       |    | RelativeTo   |
		| (ModelNode) |   | (ModelNode) |    | (AnchorNode) |
		+-------------+   +-------------+    +--------------+
		                                               |
		 -----------------------------------------------
		 |                    |                        |
		+-------------+   +-------------+   More RelativeTo...
		| Model       |   | Model       |
		| (ModelNode) |   | (ModelNode) |
		+-------------+   +-------------+
	 */
	private fun handleARML() {
		Log.d(TAG, arml.toString())

		val arElements : List<ARElement> = arml.elements

		arElements.forEach {
			when(it) {
				is Feature -> handleFeature(it)
				is Trackable -> handleTrackable(it)
			}
		}

		// disable any update requested during processing
		updateSceneRequested = false


		// == Init SceneView for ARML ==

		// Detect and assign anchors to elements in queue
		thingsToDo.add { _, frame ->
			frame.getUpdatedPlanes()
				.firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
				?.let { plane ->
					queuedAnchors.forEach {
						addAnchorNodeToScene(plane.createAnchor(plane.centerPose), it)
						Log.d(TAG, "Assigned anchor to $it")

						// Relative
						copyAnchorNode(it)
					}
					queuedAnchors.clear()
				}
		}

		// Check conditions
		thingsToDo.add { _, _ ->
			conditionalAssets.forEach {
				if (!placedAssets.contains(it.key)) {
					Log.e(TAG, "${it.key} in conditionalAssets but not in placedAssets")
				} else if (!modelNodes.contains(it.key)) {
					Log.e(TAG, "${it.key} in conditionalAssets but not in modelNodes")
				} else {
					val asset = it.key
					val anchorNode = placedAssets[asset]!!
					val conditions = it.value
					val modelNode = modelNodes[asset]!!

					val before = anchorNode.childNodes.contains(modelNode)
					val after = evaluateConditions(asset, conditions)

					if (before != after) {
						Log.d(TAG, "${if (!after) "Hiding" else "Showing"} $asset")

						if (after) {
							anchorNode.addChildNode(modelNode)
						} else {
							anchorNode.removeChildNode(modelNode)
						}
					}
				}
			}
		}

		// Refresh scene when requested
		// Only add this at the end
		// clearScene clears thingsToDo list, so there ~~can~~ will be a problem with concurrency
		thingsToDo.add { _, _ ->
			if (updateSceneRequested) {
				isLoading = true
				clearScene()
				handleARML()
				isLoading = false
				updateSceneRequested = false
			}
		}
	}

	private fun handleFeature(feature: Feature) {
		Log.d(TAG, "Got Feature $feature")
		if (feature.enabled == false) return
		val anchors = feature.anchors

		anchors.forEach {
			when(it) {
				is Trackable -> if (it.enabled != false) handleTrackable(it)
				is RelativeTo -> if (it.enabled != false) handleRelativeTo(it)
				is ScreenAnchor -> if (it.enabled != false) Log.d(TAG, "Got ScreenAnchor $it. Ignoring...")
				is Geometry -> if (it.enabled != false) Log.d(TAG, "Got Geometry $it. Ignoring...")

				is LowLevelFeature.FeatureAnchors.AnchorRef -> arml.elementsById[it.href].let {
					when (it) {
						is Trackable -> if (it.enabled != false) handleTrackable(it)
						is RelativeTo -> if (it.enabled != false) handleRelativeTo(it)
						is ScreenAnchor -> if (it.enabled != false) Log.d(TAG, "Got ScreenAnchor $it. Ignoring...")
						is Geometry -> if (it.enabled != false) Log.d(TAG, "Got Geometry $it. Ignoring...")
					}
				}
			}
		}
	}

	private fun handleTrackable(trackable: Trackable) {
		Log.d(TAG, "Got Trackable $trackable")
		val configList = trackable.config.sortedBy { it.order }

		configList.forEach {
			if (it.tracker == "#genericPlaneTracker") {
				if (!assignedAnchors.containsKey(trackable) && !queuedAnchors.contains(trackable))
					queuedAnchors.add(trackable)
					Log.d(TAG, "Waiting for anchor for $trackable")
			}
		}
	}

	private fun handleRelativeTo(relativeTo: RelativeTo) {
		Log.d(TAG, "Got RelativeTo $relativeTo")

		val other = arml.elementsById[relativeTo.ref]
		if (other == null) {
			Log.e(TAG, "RelativeTo $relativeTo trying to reference an element that does not exist (yet?).")
			return
		}

		when(other) {
			is Trackable -> {
				if (assignedAnchors.containsKey(other)) {
					val otherAnchorNode = assignedAnchors[other]!!
					addRelativeAnchorNode(relativeTo, otherAnchorNode)
					Log.d(TAG, "Created $relativeTo")
				} else {
					queuedRelativeAnchors.putIfAbsent(other, ArrayList())
					queuedRelativeAnchors[other]!!.add(relativeTo)
					Log.d(TAG, "Waiting for anchor for $relativeTo, aka $other")
				}
			}
			is RelativeTo -> {
				if (assignedAnchors.containsKey(other)) {
					val otherAnchorNode = assignedAnchors[other]!!
					addRelativeAnchorNode(relativeTo, otherAnchorNode)
					Log.d(TAG, "Created $relativeTo")
				} else {
					queuedRelativeAnchors.putIfAbsent(other, ArrayList())
					queuedRelativeAnchors[other]!!.add(relativeTo)
					Log.d(TAG, "Waiting for anchor for $relativeTo, aka $other")
				}
			}
			else -> Log.e(TAG, "Got a RelativeTo referencing $other. That type is not supported for now.")
		}
	}

	private fun addAnchorNodeToScene(anchor: Anchor, trackable: Trackable) {
		val sceneView = binding.sceneView
		sceneView.addChildNode(
			AnchorNode(sceneView.engine, anchor)
				.apply {
					trackable.assets.forEach {
						when (it) {
							is Model -> loadAndAddChildNode(this, it)
						}
					}
					assignedAnchors[trackable] = this
				}
		)
	}

	private fun copyAnchorNode(original: com.simplemobiletools.camera.ar.arml.elements.Anchor) {
		if (queuedRelativeAnchors.containsKey(original)) {
			val originalAnchorNode = assignedAnchors[original]!!

			queuedRelativeAnchors[original]!!.forEach { new ->
				addRelativeAnchorNode(new, originalAnchorNode)
				Log.d(TAG, "Assigned anchor to $new")

				// Call recursively
				copyAnchorNode(new)
			}

			queuedRelativeAnchors.remove(original)
		}
	}

	private fun addRelativeAnchorNode(relativeTo: RelativeTo, other: AnchorNode) {
		val sceneView = binding.sceneView

		val otherPos = other.position
		val newPos = relativeTo.geometry.let {
			when(it) {
				is Point -> {
					//Let's assume 3 dimensions
					val coords = it.pos
					otherPos.plus(Float3(coords[0], coords[1], coords[2]))
				}
				else -> Position(0f,0f,0f)
			}
		}

		val newAnchorNode = AnchorNode(sceneView.engine, other.anchor)
			.apply {
				other.addChildNode(this)
				position = newPos
				relativeTo.assets.forEach {
					when (it) {
						is Model -> loadAndAddChildNode(this, it)
					}
					assignedAnchors[relativeTo] = this
				}
			}
	}

	private fun loadAndAddChildNode(anchor: AnchorNode, model: Model) {
		val sceneView = binding.sceneView

		val modelPath = model.href
		val scale = model.scaleVector
		val rotation = model.rotationVector

		val conditions = model.conditions

		anchor.apply {
			isEditable = true
			lifecycleScope.launch {
				isLoading = true
				sceneView.modelLoader.loadModelInstance(
					modelPath
				)?.let { modelInstance ->
					val modelNode = ModelNode(
						modelInstance = modelInstance,
						scaleToUnits = null,
						centerOrigin = Position(0f,0f,0f)
					).apply {
						isEditable = true
						this.scale = this.scale.times(scale)
						this.rotation = this.rotation.plus(rotation)

						placedAssets[model] = anchor
						modelNodes[model] = this
						isSelected[model] = false
						anchor.onDoubleTap = { _ ->
							isSelected[model] = !isSelected[model]!!
							Log.d(TAG, "${if (isSelected[model]!!) "Selected" else "Unselected" } $model")
							isSelected[model]!!
						}

						if (conditions != null) {
							conditionalAssets[model] = conditions
							Log.d(TAG, "Added $model to conditionalAssets")
						}
					}

					if (conditions != null) {
						if (evaluateConditions(model, conditions))
							addChildNode(modelNode)
					} else {
						addChildNode(modelNode)
					}
					Log.d(TAG, "Placed $model")
				}
				isLoading = false
			}
		}
	}

	private fun evaluateConditions(asset: VisualAsset, conditions: List<Condition>): Boolean {
		conditions.forEach {
			when(it) {
				is SelectedCondition -> if (!evaluateSelectedCondition(asset, it)) return false
				is DistanceCondition -> if (!evaluateDistanceCondition(asset, it)) return false
			}
		}
		return true
	}

	private fun evaluateSelectedCondition(asset: VisualAsset, condition: SelectedCondition): Boolean {
		return isSelected[asset] == condition.selected
	}

	private fun evaluateDistanceCondition(asset: VisualAsset, condition: DistanceCondition): Boolean {
		val sceneView = binding.sceneView

		val modelPose: Pose = placedAssets[asset]!!.anchor.pose

		val distance = sceneView.cameraNode.pose?.distanceTo(modelPose)
			.let {
				//Log.d(TAG, "Distance: $it ($asset)");
				return@let it
			} ?: let {
				//Log.d(TAG, "Distance: unavailable ($asset)");
				return false
			}

		if (condition.min != null)
			if (distance < condition.min)
				return false

		if (condition.max != null)
			if (distance > condition.max)
				return false

		return true
	}

	private fun clearScene() {
		Log.d(TAG, "Clearing scene...")
		val sceneView = binding.sceneView
		queuedAnchors.clear()
		assignedAnchors.values.forEach {
			it.clearChildNodes()
		}
		assignedAnchors.clear()
		thingsToDo.clear()
		sceneView.clearChildNodes()

		modelNodes.clear()
		placedAssets.clear()
		isSelected.clear()
		conditionalAssets.clear()
	}
}
