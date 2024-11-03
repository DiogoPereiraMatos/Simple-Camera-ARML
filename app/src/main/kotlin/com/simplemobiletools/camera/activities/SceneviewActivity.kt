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
import java.util.EnumMap


class SceneviewActivity : SimpleActivity() {

	companion object {
		const val DISTANCE_ROLLING_AVG_N = 3
		const val TAG = "ARML"
	}

	private val binding by viewBinding(ActivitySceneviewBinding::inflate)

	private var armlPath : String = "armlexamples/empty.xml"
		// On update, read new arml content
		set(value) {
			if (field != value) {
				Log.d(TAG, "Set armlPath to $value")
				armlContent = assets.open(value).readBuffer().array().decodeToString()
				updateSceneRequested = true
			}
			field = value
		}
	private var armlContent : String = ARMLParser.EMPTY
		// On update, parse new ARML
		set(value) {
			if (field != value) {
				try {
					val result : ARML = ARMLParser().loads(value)

					val validation = result.validate()
					if (!validation.first) {
						Log.e(TAG, "Invalid ARML: ${validation.second}")
						arml = ARML()
					}
					arml = result
				} catch (e : Exception) {
					Log.e(TAG, "Failed to parse ARML.", e)
					arml = ARML()
				}
				Log.d(TAG, "Set arml to $arml")
				updateSceneRequested = true
			}
			field = value
		}
	private var arml : ARML = ARML()

	private var updateSceneRequested : Boolean = false

	private var isLoading = false
		set(value) {
			field = value
			binding.loadingView.isGone = !value
		}

	private var trackingFailureReason: String? = null
		set(value) {
			if (field != value) {
				field = value
				updateInstructions()
			}
		}

	private fun updateInstructions() {
		binding.instructionText.text = trackingFailureReason
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		useDynamicTheme = false
		super.onCreate(savedInstanceState)
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		supportActionBar?.hide()

		setContentView(binding.root)

		// UI stuff...
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

		// Setup
		binding.apply {
			setupSceneView()
			setupButtons()
		}

		// ARML
		isLoading = true
		armlContent = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ARMLParser.EMPTY
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
			planeRenderer.isEnabled = true
			configureSession { session, config ->
				config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
					true -> Config.DepthMode.AUTOMATIC
					else -> Config.DepthMode.DISABLED
				}
				config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
				config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
			}
			onSessionUpdated = { session, frame -> thingsToDo.forEach {it(session, frame)} }
			onTrackingFailureChanged = { reason ->
				this@SceneviewActivity.trackingFailureReason = reason?.getDescription(applicationContext)
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

	private fun launchARMLSettingsMenuDialog() {
		val strings = listAssets("armlexamples")!!
			.toTypedArray()

		AlertDialog.Builder(this)
			.setTitle("ARML")
			.setSingleChoiceItems(strings, strings.indexOf(armlPath)) { _, which -> armlPath = strings[which] }
			.show()
	}

	private fun launchSettings() {
		Intent(this, SettingsActivity::class.java).also {
			startActivity(it)
		}
	}


	//=== ARML ===//

	/*
                                                            +----------+
                                                            |   ARML   |
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
	          ARML        |      SceneView
		+-------------+   |   +-------------+
		|     ARML    |   |   |    Scene    |
		+-------------+   |   +-------------+
		       |          |          |
		       v          |          |
       (Anchor as in:     |          |
        - Trackable       |          |
        - ScreenAnchor    |          |                                       (detected by arcore)
        - ...)		      |          v       (com.google.ar.core.Anchor)  (com.google.ar.core.Trackable)
		+-------------+   |   +-------------+     +-------------+          +----------------------+
		|   Anchor    |   |   | AnchorNode  | <-- |    Anchor   | <------- | Trackable (eg Plane) |
		+-------------+   |   +-------------+     +-------------+          +----------------------+
		       |          |          |
		       |          |          | (if placed / shown)
		       v          |          v
		+-------------+   |   +-------------+
		|Model/V.Asset|   |   |  ModelNode  |
		+-------------+   |   +-------------+
	 */

	// Trackables waiting for an AnchorNode go in here vvv
	private val queuedAnchors : EnumMap<Plane.Type, ArrayList<Trackable>> = EnumMap(Plane.Type::class.java)

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
		queuedAnchors.values.forEach { it.clear() }
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

		// Initialize queues
		Plane.Type.entries.forEach { type ->
			queuedAnchors.putIfAbsent(type, ArrayList())
		}

		// Process ARML
		val arElements : List<ARElement> = arml.elements
		arElements.forEach {
			when(it) {
				is Feature -> handleFeature(it)
				is Trackable -> handleTrackable(it)
				else -> Log.w(TAG, "Element is not supported: $it")
			}
		}

		// OnFrameUpdate: Detect and assign anchors to elements in queue
		thingsToDo.add { _, frame ->
			val planes = frame.getUpdatedPlanes()
			assignPlanes(planes)
		}

		// OnFrameUpdate: Check conditions
		thingsToDo.add { _, _ -> conditionalVisualAssets.forEach { checkCondition(it) } }

		// OnFrameUpdate: Refresh scene when requested
		// Only add this at the end. clearScene clears thingsToDo list, so there will be a problem with concurrency
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

	private fun assignPlanes(planes: Collection<Plane>) {
		val types = Plane.Type.entries

		types.forEach { type ->
			val plane = planes.firstOrNull { it.type == type }
			plane?.let { it ->
				queuedAnchors[type]!!.forEach { trackable ->
					addAnchorNodeToScene(plane.createAnchor(plane.centerPose), trackable)
					Log.d(TAG, "Assigned anchor to $it")
					copyAnchorNode(trackable) // Relative
				}
				queuedAnchors[type]!!.clear()
			}
		}
	}

	private fun checkCondition(asset: VisualAsset) {
		val conditions : List<Condition> = asset.conditions!!

		if (asset.anchorNode == null || asset.modelNode == null) {
			Log.e(TAG, "$asset has conditions but has no anchorNode or modelNode")
			return
		}

		val before = asset.isPlaced
		val after = evaluateConditions(asset, conditions)

		if (before != after)
			asset.setVisibility(after)
	}

	private fun handleFeature(feature: Feature) {
		Log.d(TAG, "Got Feature $feature")
		if (feature.enabled == false) return
		feature.anchors.forEach { handleAnchor(it) }
	}

	private fun handleAnchor(anchor: Any) {
		when(anchor) {
			is Trackable -> if (anchor.enabled != false) handleTrackable(anchor)
			is RelativeTo -> if (anchor.enabled != false) handleRelativeTo(anchor)
			is ScreenAnchor -> if (anchor.enabled != false) handleScreenAnchor(anchor)
			is Geometry -> if (anchor.enabled != false) handleGeometry(anchor)
			is LowLevelFeature.FeatureAnchors.AnchorRef -> handleAnchorRef(anchor)
		}
	}

	private fun ArrayList<Trackable>.putIfAbsent(element: Trackable) {
		if (!this.contains(element)) {
			this.add(element)
		}
	}

	private fun handleTrackable(trackable: Trackable) {
		Log.d(TAG, "Got Trackable $trackable")
		val configList = trackable.config.sortedBy { it.order }

		val trackerMap = HashMap<String, Plane.Type>()
		trackerMap["#genericPlaneTracker"] = Plane.Type.HORIZONTAL_UPWARD_FACING
		trackerMap["#genericHUPlaneTracker"] = Plane.Type.HORIZONTAL_UPWARD_FACING
		trackerMap["#genericHDPlaneTracker"] = Plane.Type.HORIZONTAL_DOWNWARD_FACING
		trackerMap["#genericVPlaneTracker"] = Plane.Type.VERTICAL

		configList.forEach {
			val planeType = trackerMap[it.tracker]
			queuedAnchors[planeType]!!.putIfAbsent(trackable)
			Log.d(TAG, "Waiting for anchor for $trackable")
		}
	}

	private fun handleRelativeTo(relativeTo: RelativeTo) {
		Log.d(TAG, "Got RelativeTo $relativeTo")

		val other = arml.elementsById[relativeTo.ref]
		if (other == null) {
			Log.w(TAG, "RelativeTo $relativeTo trying to reference an element that does not exist (yet?).")
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
			else -> Log.w(TAG, "Got a RelativeTo referencing $other. That type is not supported for now.")
		}
	}

	//TODO
	private fun handleScreenAnchor(screenAnchor: ScreenAnchor) {
		Log.w(TAG, "Got ScreenAnchor $screenAnchor. Ignoring...")
	}

	//TODO
	private fun handleGeometry(geometry: Geometry) {
		Log.w(TAG, "Got Geometry $geometry. Ignoring...")
	}

	private fun handleAnchorRef(ref: LowLevelFeature.FeatureAnchors.AnchorRef) {
		if (arml.elementsById.containsKey(ref.href))
			handleAnchor(arml.elementsById[ref.href]!!)
		else {
			Log.w(TAG, "Reference to unknown Anchor: ${ref.href}. Ignoring...")
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
