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
import com.simplemobiletools.camera.R
import com.simplemobiletools.camera.ar.arml.ARMLParser
import com.simplemobiletools.camera.ar.arml.elements.*
import com.simplemobiletools.camera.ar.arml.elements.Trackable
import com.simplemobiletools.camera.ar.arml.elements.Anchor
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
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.ModelNode
import io.github.sceneview.utils.readBuffer
import kotlinx.coroutines.launch


class SceneviewActivity : SimpleActivity() {

	companion object {
		const val DISTANCE_ROLLING_AVG_N = 3
		const val TAG = "ARML"
	}

	private val binding by viewBinding(ActivitySceneviewBinding::inflate)

	private var myModelPath : String = "models/damaged_helmet.glb"
		set(value) {
			if (field != value) {
				Log.d(TAG, "Set virtualObjectName to $value")
				updateSceneRequested = true
			}
			field = value
		}
	private var armlPath : String = "armlexamples/empty.xml"
		set(value) {
			if (field != value) {
				Log.d(TAG, "Set armlPath to $value")
				updateSceneRequested = true
			}
			field = value
		}
	private var updateSceneRequested : Boolean = false

	private var isLoading = false
		set(value) {
			field = value
			binding.loadingView.isGone = !value
		}

	private var trackingFailureReason: TrackingFailureReason? = null
		set(value) {
			if (field != value) {
				field = value
				updateInstructions()
			}
		}

	private fun updateInstructions() {
		binding.instructionText.text = trackingFailureReason?.getDescription(this)
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
	@Suppress("SuspiciousVarProperty")
	private var armlContent : String = ARMLParser.EMPTY
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

	/*
                                                            +----------+
                                                            |  Scene   |
                                                            +----------+
                                                                  1
                                                                  |
                                                                0..*
                                        -------------------------------------------------------------   ...
                                        |                                 |
                                +---------------+                 +---------------+
                                | Trackable     |                 | Trackable     |
                                | (Anchor Node) |                 | (Anchor Node) |
                                +---------------+                 +---------------+
                                      0..1
                                        |
                                      0..*
               --------------------------------------------------------------   ...
               |                    |                        |
        +-------------+      +-------------+          +--------------+
        | Model       |      | Model       |          | RelativeTo   |
        | (ModelNode) |      | (ModelNode) |          | (AnchorNode) |
        +-------------+      +-------------+          +--------------+
                                                             |
                                      -----------------------------------------------
                                      |                    |                        |
                                +-------------+      +-------------+       More RelativeTo...
                                | Model       |      | Model       |
                                | (ModelNode) |      | (ModelNode) |
                                +-------------+      +-------------+
	 */

	/*
	          ARML        |     SceneView
		                  |   +-------------+
		                  |   |    Scene    |
		                  |   +-------------+
		                  |          |
		                  |          v
		                  |   +-------------+
		                  |   |    Anchor   | (created from planes; denoted as com.google.ar.core.Anchor)
		                  |   +-------------+
		                  |          |
		                  |          v
		+-------------+   |   +-------------+
		|   Anchor    |   |   | AnchorNode  |
		+-------------+   |   +-------------+
		       |          |          |
		       |          |          | (if placed / shown)
		       v          |          v
		+-------------+   |   +-------------+
		|Model/V.Asset|   |   |  ModelNode  |
		+-------------+   |   +-------------+
	 */

	// Trackables waiting for an AnchorNode go in here vvv
	private val queuedAnchorsHU : ArrayList<Trackable> = ArrayList()
	private val queuedAnchorsHD : ArrayList<Trackable> = ArrayList()
	private val queuedAnchorsV : ArrayList<Trackable> = ArrayList()

	// RelativeTo waiting for Anchors go in here vvv
	private val queuedRelativeAnchors : HashMap<Anchor, ArrayList<RelativeTo>> = HashMap()

	// Save AnchorNodes of assigned Trackables and RelativeTo
	private val assignedAnchors : HashMap<Anchor, AnchorNode> = HashMap()
	private val Anchor.anchorNode : AnchorNode?
		get() = this@SceneviewActivity.assignedAnchors[this]
	private fun Anchor.setAnchorNode(anchorNode: AnchorNode) {
		this@SceneviewActivity.assignedAnchors[this] = anchorNode
	}

	// Save VisualAssets that have conditions
	private val conditionalVisualAssets : ArrayList<VisualAsset> = ArrayList()

	// Save ModelNodes of VisualAssets
	private val modelNodes : HashMap<VisualAsset, ModelNode> = HashMap()
	private val VisualAsset.modelNode : ModelNode?
		get() = this@SceneviewActivity.modelNodes[this]
	private fun VisualAsset.setModelNode(modelNode : ModelNode) {
		this@SceneviewActivity.modelNodes[this] = modelNode
	}

	// Save AnchorNodes of VisualAssets
	private val anchorNodes : HashMap<VisualAsset, AnchorNode> = HashMap()
	private val VisualAsset.anchorNode : AnchorNode?
		get() = this@SceneviewActivity.anchorNodes[this]
	private fun VisualAsset.setAnchorNode(anchorNode : AnchorNode) {
		this@SceneviewActivity.anchorNodes[this] = anchorNode
	}

	// Asset is placed only when the ModelNode is a child of the AnchorNode
	// There is no function (that I know of) that hides and shows model on command, so this is how I'll do it...
	private val VisualAsset.isPlaced : Boolean
		get() {
			return if (anchorNode != null && modelNode != null)
				anchorNode!!.childNodes.contains(modelNode!!)
			else false
		}
	private fun VisualAsset.show() {
		if (isPlaced) {
			Log.w(TAG, "Tried to show asset that is already shown: $this")
			return
		}
		if (anchorNode != null && modelNode != null) {
			anchorNode!!.addChildNode(modelNode!!)
			Log.d(TAG, "Showing $this")
		}
	}
	private fun VisualAsset.hide() {
		if (!isPlaced) {
			Log.w(TAG, "Tried to hide asset that is already hidden: $this")
			return
		}
		if (anchorNode != null && modelNode != null) {
			anchorNode!!.removeChildNode(modelNode!!)
			Log.d(TAG, "Hiding $this")
		}
	}
	private fun VisualAsset.setVisibility(shown : Boolean) {
		if (shown)
			show()
		else
			hide()
	}

	// SelectedCondition: Keep track of what assets are selected
	private val isSelected : HashMap<VisualAsset, Boolean> = HashMap()
	private var VisualAsset.isSelected : Boolean
		get() = this@SceneviewActivity.isSelected[this] ?: false
		set(value) = this@SceneviewActivity.isSelected.set(this, value)
	private fun VisualAsset.toggleSelected() {
		this.isSelected = !this.isSelected
		Log.d(TAG, "${if (this.isSelected) "Selected" else "Unselected" } $this")
	}

	// DistanceCondition: Keep track of the rolling average distance of the asset
	private val averageDistance : HashMap<VisualAsset, Float> = HashMap()
	private var VisualAsset.avgDistance : Float
		get() = this@SceneviewActivity.averageDistance[this] ?: -1f
		set(value) = this@SceneviewActivity.averageDistance.set(this, value)
	private fun VisualAsset.newDistance(d: Float) {
		this.avgDistance = (this.avgDistance * (DISTANCE_ROLLING_AVG_N-1) + d) / DISTANCE_ROLLING_AVG_N
	}

	// Keep track of functions to execute every frame (e.g. checking conditions)
	private val thingsToDo : ArrayList<((Session, Frame) -> Unit)> = ArrayList()

	private fun clearScene() {
		val sceneView = binding.sceneView

		Log.d(TAG, "Clearing scene...")

		// This first
		thingsToDo.clear()

		// Clear queues
		queuedAnchorsHU.clear()
		queuedAnchorsHD.clear()
		queuedAnchorsV.clear()
		queuedRelativeAnchors.clear()

		// Clear scene nodes
		assignedAnchors.values.forEach { it.clearChildNodes() }
		assignedAnchors.clear()
		sceneView.clearChildNodes()

		// Clear auxiliary stuff
		conditionalVisualAssets.clear()
		modelNodes.clear()
		anchorNodes.clear()
		isSelected.clear()

		Log.d(TAG, "Clearing scene... DONE!")
	}



	private fun handleARML() {
		Log.d(TAG, arml.toString())

		// Disable any update requested during processing
		updateSceneRequested = false

		// Process ARML
		val arElements : List<ARElement> = arml.elements
		arElements.forEach {
			when(it) {
				is Feature -> handleFeature(it)
				is Trackable -> handleTrackable(it)
			}
		}

		// OnFrameUpdate: Detect and assign anchors to elements in queue
		thingsToDo.add { _, frame ->
			val planes = frame.getUpdatedPlanes()

			val planeHU = planes.firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
			val planeHD = planes.firstOrNull { it.type == Plane.Type.HORIZONTAL_DOWNWARD_FACING }
			val planeV = planes.firstOrNull { it.type == Plane.Type.VERTICAL }

			planeHU?.let { plane ->
				queuedAnchorsHU.forEach {
					addAnchorNodeToScene(plane.createAnchor(plane.centerPose), it)
					Log.d(TAG, "Assigned anchor to $it")
					copyAnchorNode(it) // Relative
				}
				queuedAnchorsHU.clear()
			}

			planeHD?.let { plane ->
				queuedAnchorsHD.forEach {
					addAnchorNodeToScene(plane.createAnchor(plane.centerPose), it)
					Log.d(TAG, "Assigned anchor to $it")
					copyAnchorNode(it) // Relative
				}
				queuedAnchorsHD.clear()
			}

			planeV?.let { plane ->
				queuedAnchorsV.forEach {
					addAnchorNodeToScene(plane.createAnchor(plane.centerPose), it)
					Log.d(TAG, "Assigned anchor to $it")
					copyAnchorNode(it) // Relative
				}
				queuedAnchorsV.clear()
			}
		}

		// OnFrameUpdate: Check conditions
		thingsToDo.add { _, _ ->
			conditionalVisualAssets.forEach {

				val asset : VisualAsset = it
				val conditions : List<Condition> = it.conditions!!

				if (asset.anchorNode == null || asset.modelNode == null) {
					Log.e(TAG, "$asset has conditions but has no anchorNode or modelNode")
					return@forEach
				}

				val before = asset.isPlaced
				val after = evaluateConditions(asset, conditions)

				if (before != after)
					asset.setVisibility(after)
			}
		}

		// OnFrameUpdate: Refresh scene when requested
		// Only add this at the end
		// clearScene clears thingsToDo list, so there can and will be a problem with concurrency
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

				is LowLevelFeature.FeatureAnchors.AnchorRef -> arml.elementsById[it.href].let { ref ->
					when (ref) {
						is Trackable -> if (ref.enabled != false) handleTrackable(ref)
						is RelativeTo -> if (ref.enabled != false) handleRelativeTo(ref)
						is ScreenAnchor -> if (ref.enabled != false) Log.d(TAG, "Got ScreenAnchor $ref. Ignoring...")
						is Geometry -> if (ref.enabled != false) Log.d(TAG, "Got Geometry $ref. Ignoring...")
					}
				}
			}
		}
	}

	private fun handleTrackable(trackable: Trackable) {
		Log.d(TAG, "Got Trackable $trackable")
		val configList = trackable.config.sortedBy { it.order }

		configList.forEach {
			if (it.tracker == "#genericPlaneTracker" || it.tracker == "#genericHUPlaneTracker") {
				if (!assignedAnchors.containsKey(trackable) && !queuedAnchorsHU.contains(trackable))
					queuedAnchorsHU.add(trackable)
			}
			if (it.tracker == "#genericHDPlaneTracker") {
				if (!assignedAnchors.containsKey(trackable) && !queuedAnchorsHD.contains(trackable))
					queuedAnchorsHD.add(trackable)
			}
			if (it.tracker == "#genericVPlaneTracker") {
				if (!assignedAnchors.containsKey(trackable) && !queuedAnchorsV.contains(trackable))
					queuedAnchorsV.add(trackable)
			}
			Log.d(TAG, "Waiting for anchor for $trackable")
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
					val otherAnchorNode = other.anchorNode!!
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
					val otherAnchorNode = other.anchorNode!!
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

	private fun addAnchorNodeToScene(anchor: com.google.ar.core.Anchor, trackable: Trackable) {
		val sceneView = binding.sceneView

		sceneView.addChildNode(
			AnchorNode(sceneView.engine, anchor)
				.apply {
					trackable.assets.forEach {
						when (it) {
							is Model -> loadAndAddChildNode(this, it)
						}
					}
					trackable.setAnchorNode(this)
				}
		)
	}

	private fun copyAnchorNode(original: Anchor) {
		if (queuedRelativeAnchors.containsKey(original)) {
			val originalAnchorNode = original.anchorNode!!

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
				isEditable = true
				other.addChildNode(this)
				position = newPos
				relativeTo.assets.forEach {
					when (it) {
						is Model -> loadAndAddChildNode(this, it)
					}
					relativeTo.setAnchorNode(this)
				}
			}
	}

	private fun addModelToScene(anchorNode: AnchorNode, modelNode: ModelNode, model: Model) {
		model.setAnchorNode(anchorNode)
		model.setModelNode(modelNode)
		model.isSelected = false
		modelNode.onDoubleTap = { _ ->	model.toggleSelected(); true;}

		if (model.conditions != null)
		{
			conditionalVisualAssets.add(model)
			Log.d(TAG, "Added $model to conditionalVisualAssets")
			if (evaluateConditions(model, model.conditions!!)) {
				anchorNode.addChildNode(modelNode)
				Log.d(TAG, "Placed $model")
			}
		}
		else
		{
			anchorNode.addChildNode(modelNode)
			Log.d(TAG, "Placed $model")
		}
	}

	private fun loadAndAddChildNode(anchorNode: AnchorNode, model: Model) {
		val sceneView = binding.sceneView

		lifecycleScope.launch {
			isLoading = true

			val modelInstance : ModelInstance? = sceneView.modelLoader.loadModelInstance(model.href)

			modelInstance?.let {
				val modelNode = ModelNode(
					modelInstance = it,
					scaleToUnits = null,
					centerOrigin = Position(0f,0f,0f)
				).apply {
					isEditable = true
					this.scale = this.scale.times(model.scaleVector)
					this.rotation = this.rotation.plus(model.rotationVector)

					addModelToScene(
						anchorNode = anchorNode,
						modelNode = this,
						model = model
					)
				}
			}

			isLoading = false
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
		return asset.isSelected == condition.selected
	}

	private fun evaluateDistanceCondition(asset: VisualAsset, condition: DistanceCondition): Boolean {
		val sceneView = binding.sceneView

		val modelPose: Pose = asset.anchorNode!!.anchor.pose

		var distance = sceneView.cameraNode.pose?.distanceTo(modelPose)
			.let {
				//Log.d(TAG, "Distance: $it ($asset)");
				return@let it
			} ?: let {
				//Log.d(TAG, "Distance: unavailable ($asset)");
				return false
			}
		//asset.newDistance(distance)
		//distance = asset.avgDistance

		// Smooth transitions; Avoid showing and hiding model repeatedly; Like a rubber band
		val deadZone = 0.2

		/*
			0 ----- Hidden ----- | *(1-deadzone) | Min | *(1+deadzone) | ----- Shown ----- | ...

			- If shown, hide when <*(1-deadzone)
			- If hidden, show when >*(1+deadzone) => keep hidden while <*(1+deadzone)
		 */
		if (condition.min != null)
			if (distance < (if (asset.isPlaced) condition.min *(1-deadZone) else condition.min * (1+deadZone)))
				return false

		/*
			... ----- Shown ----- | *(1-deadzone) | Max | *(1+deadzone) | ----- Hidden ----- | ...

			- If shown, hide when >*(1+deadzone)
			- If hidden, show when <*(1-deadzone) => keep hidden while >*(1-deadzone)
		 */
		if (condition.max != null)
			if (distance > (if (asset.isPlaced) condition.max *(1+deadZone) else condition.max * (1-deadZone)))
				return false

		return true
	}

	// Log filter:
	// package:mine ARML | Analyzer  | QR | (level:error & -tag:Camera & -tag:OpenGLRenderer & -message:static_feature & -message:bundle_adjustment & -message:hit_test & -message:depth & -message:IMU & -message:landmarks & -message:stream)
}
