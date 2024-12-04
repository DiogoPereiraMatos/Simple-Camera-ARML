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
import com.simplemobiletools.camera.ar.arml.elements.Anchor
import com.simplemobiletools.camera.ar.arml.elements.Trackable
import com.simplemobiletools.camera.ar.arml.elements.gml.LineString
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
import io.github.sceneview.model.setBlendOrder
import io.github.sceneview.model.setGlobalBlendOrderEnabled
import io.github.sceneview.node.ModelNode
import io.github.sceneview.utils.readBuffer
import kotlinx.coroutines.launch
import java.util.EnumMap
import kotlin.math.absoluteValue


//DONE:
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/parse_encoding
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/Feature/enabled
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/Anchor/enabled
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/Trackable_And_Tracker/unknown_tracker
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/Trackable_And_Tracker/config_order_max
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/VisualAsset/enabled
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/Label/hyperlinkBehavior_default
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/Label/viewportWidth_default
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/Model/type_default
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/Scale/defaults
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/Scaling_VisualAssets/minScalingDistance_default
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/Condition/multiple
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/Condition/Distance/max
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/Condition/Distance/min
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/Condition/Distance/min_and_max
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/Condition/Selected/listener_default
//REQ: http://www.opengis.net/spec/arml/2.0/req/core/Condition/Selected/selected

//TODO:
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/units
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/ARElement/id_user
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Anchor/anchor_without_feature
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/ARAnchor/no_visual_asset
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Geometry/no_position
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/GMLGeometries/crs
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/GMLGeometries/default_crs
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/GMLGeometries/no_altitude
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/GMLGeometries/LineString/definition
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/GMLGeometries/LinearRing/definition
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/GMLGeometries/Polygon/definition
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/GMLGeometries/local_cs/cs_type
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/GMLGeometries/local_cs/cs_type/Polygon
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Trackable_And_Tracker/contained_trackable
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Trackable_And_Tracker/trackable_2D_size
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Trackable_And_Tracker/trackable_3D_size
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Trackable_And_Tracker/trackable_size_preset
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Trackable_And_Tracker/trackable_missing_size
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/RelativeTo/GMLGeometry_properties
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/ScreenAnchor/property_conflicts
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/ScreenAnchor/missing_properties
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/ScreenAnchor/ignored_properties
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/ScreenAnchor/default_properties
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/VisualAsset/projection_order
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/VisualAsset2D/width_and_heigh
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/VisualAsset2D/orientationMode
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/VisualAsset2D/backSide
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Label/href_and_src_precedence
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Label/href_and_src_required
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Label/hyperlinkBehavior
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Label/metadata_name_description
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Label/metadata_general
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Fill/color_default
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Text/metadata_name_description
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Text/metadata_general
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Text/font‐color_default
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Text/background‐color_default
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Image/formats
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Model/formats
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Model/type
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Scale/axis
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/AutomaticOrientation_VisualAssets/2D
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/AutomaticOrientation_VisualAssets/3D_dim_0
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/AutomaticOrientation_VisualAssets/3D_dim_1_2
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/ManualOrientation_VisualAssets/order
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/ManualOrientation_VisualAssets/axes
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/ManualOrientation_VisualAssets/application
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Scaling_VisualAssets/minMaxScalingDistance
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Scaling_VisualAssets/scalingFactor
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Scaling_VisualAssets/minScalingDistance_default
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Scaling_VisualAssets/maxScalingDistance_ignored
//TODO: REQ: http://www.opengis.net/spec/arml/2.0/req/core/Scaling_VisualAssets/scalingFactor_ignored
//TODO: REQ:

//TODO: Define http://www.opengis.net/arml /tracker/genericImageTracker

private fun ArrayList<Trackable>.putIfAbsent(element: Trackable) {
	if (!this.contains(element)) {
		this.add(element)
	}
}

class SceneviewActivity : SimpleActivity() {

	companion object {
		const val DISTANCE_ROLLING_AVG_N = 3
		const val DEADZONE = 0.1 //%
		const val TAG = "ARML"

		val trackerMap : HashMap<String, Plane.Type> = HashMap(
			mapOf(
				Pair("#genericPlaneTracker", Plane.Type.HORIZONTAL_UPWARD_FACING),
				Pair("#genericHUPlaneTracker", Plane.Type.HORIZONTAL_UPWARD_FACING),
				Pair("#genericHDPlaneTracker", Plane.Type.HORIZONTAL_DOWNWARD_FACING),
				Pair("#genericVPlaneTracker", Plane.Type.VERTICAL),
			)
		)
	}

	private val arElementHandlers : EnumMap<ARElementType, (ARElement) -> Unit> = EnumMap(
		mapOf<ARElementType, (ARElement) -> Unit>(
			Pair(ARElementType.FEATURE)      { it as Feature; it.handle() },
			Pair(ARElementType.TRACKABLE)    { it as Anchor;  it.handle() },
			Pair(ARElementType.RELATIVETO)   { it as Anchor;  it.handle() },
			Pair(ARElementType.SCREENANCHOR) { it as Anchor;  it.handle() },
			Pair(ARElementType.GEOMETRY)     { it as Anchor;  it.handle() },
		)
	)

	private val anchorHandlers : EnumMap<ARElementType, (Anchor) -> Unit> = EnumMap(
		mapOf<ARElementType, (Anchor) -> Unit>(
			Pair(ARElementType.TRACKABLE)    { it as Trackable;    it.handle() },
			Pair(ARElementType.RELATIVETO)   { it as RelativeTo;   it.handle() },
			Pair(ARElementType.SCREENANCHOR) { it as ScreenAnchor; if (it.enabled) Log.w(TAG, "Got ScreenAnchor. Ignoring...") },
			Pair(ARElementType.GEOMETRY)     { it as Geometry;     if (it.enabled) Log.w(TAG, "Got Geometry. Ignoring...") },
		)
	)

	private val relativeToRefHandler : EnumMap<ARElementType, (RelativeToAble, RelativeTo) -> Unit> = EnumMap(
		mapOf<ARElementType, (RelativeToAble, RelativeTo) -> Unit>(
			Pair(ARElementType.TRACKABLE)  { ref, self -> ref as Trackable;  ref.handleRelativeTo(self) },
			Pair(ARElementType.RELATIVETO) { ref, self -> ref as RelativeTo; ref.handleRelativeTo(self) },
			Pair(ARElementType.GEOMETRY)   { ref, self -> ref as Geometry;   ref.handleRelativeTo(self) },
			Pair(ARElementType.MODEL)      { ref, self -> ref as Model;      if (self.enabled) Log.w(TAG, "Got a RelativeTo referencing a Model. That type hasn't been implemented.") }
		)
	)

	private val conditionHandlers : EnumMap<ARElementType, (VisualAsset, Condition) -> Boolean> = EnumMap(
		mapOf<ARElementType, (VisualAsset, Condition) -> Boolean>(
			Pair(ARElementType.SELECTEDCONDITION) { asset, condition -> condition as SelectedCondition; asset.evaluateSelectedCondition(condition) },
			Pair(ARElementType.DISTANCECONDITION) { asset, condition -> condition as DistanceCondition; asset.evaluateDistanceCondition(condition) },
		)
	)

	private val assetHandlers : EnumMap<ARElementType, (AnchorNode, VisualAsset) -> Unit> = EnumMap(
		mapOf<ARElementType, (AnchorNode, VisualAsset) -> Unit>(
			Pair(ARElementType.MODEL) { anchor, asset -> asset as Model; loadAndAddChildNode(anchor, asset) },
		)
	)

	private val binding by viewBinding(ActivitySceneviewBinding::inflate)
	private val sceneView
		get() = binding.sceneView

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
				binding.instructionText.text = trackingFailureReason
			}
		}

	override fun onCreate(savedInstanceState: Bundle?) {
		useDynamicTheme = false
		super.onCreate(savedInstanceState)
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		supportActionBar?.hide()

		setContentView(binding.root)

		// Setup
		binding.apply {
			setupWindow()
			setupSceneView()
			setupButtons()
		}

		// ARML
		isLoading = true
		armlPath = intent.getStringExtra(Intent.EXTRA_TEXT) ?: armlPath
		arml.handle()
		isLoading = false
	}

	override fun onResume() {
		super.onResume()
		if (!this.config.isArmlEnabled)
			this.finish()

		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)
	}

	override fun onPause() {
		super.onPause()
		window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
	}

	private fun setupWindow() {
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
	}

	private fun setupSceneView() {
		sceneView.apply {
			planeRenderer.isEnabled = true
			configureSession { session, config ->
				config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
				config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
				config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
					true -> Config.DepthMode.AUTOMATIC
					else -> Config.DepthMode.DISABLED
				}
			}
			onSessionUpdated = { session, frame ->
				thingsToDo.forEach {it(session, frame)}
			}
			onTrackingFailureChanged = { reason ->
				this@SceneviewActivity.trackingFailureReason = reason?.getDescription(applicationContext)
			}

			setOnGestureListener(
				//FIXME: Notify relevant node (good luck with the hit tests :P)
				onDoubleTap = { event, node ->
					Log.d(TAG, "Double tapped ( ${event.x} , ${event.y} ). Node: $node. Hit: ${sceneView.hitTestAR().toString()}")
					node?.onDoubleTap(event)
					this@SceneviewActivity.isSelected.forEach { (asset, _) -> asset.toggleSelected() }  //FIXME: For test purposes only
				}
			)
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

	private var armlPath : String = "armlexamples/empty.xml"
		// On update, read new arml content
		set(value) {
			if (field != value) {
				Log.d(TAG, "Set armlPath to $value")
				armlContent = assets.open(value).readBuffer().array().decodeToString()
				field = value
			}
		}
	private var armlContent : String = ARMLParser.EMPTY
		// On update, parse new ARML
		set(value) {
			try {
				val result : ARML = ARMLParser().loads(value)

				val validation = result.validate()
				if (!validation.first) {
					Log.e(TAG, "Invalid ARML: ${validation.second}")
				} else {
					arml = result
					field = value
				}
			} catch (e : Exception) {
				Log.e(TAG, "Failed to parse ARML.", e)
			}
		}
	private var arml : ARML = ARML()
		set(value) {
			field = value
			Log.d(TAG, "Set arml to $arml")
			updateSceneRequested = true
		}

	// Trackables waiting for an AnchorNode go in here vvv
	private val queuedAnchors : EnumMap<Plane.Type, ArrayList<Trackable>> = EnumMap(Plane.Type.entries.associateWith { ArrayList() })

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
	private val VisualAsset.isHidden: Boolean
		get() {
			return if (anchorNode != null && modelNode != null)
				!anchorNode!!.childNodes.contains(modelNode!!)
			else true
		}
	private fun VisualAsset.show() {
		if (!isHidden) {
			//Log.w(TAG, "Tried to show asset that is not hidden: $this")
			return
		}
		if (anchorNode != null && modelNode != null) {
			anchorNode!!.addChildNode(modelNode!!)
			Log.d(TAG, "Showing $this")
		}
	}
	private fun VisualAsset.hide() {
		if (isHidden) {
			//Log.w(TAG, "Tried to hide asset that is already hidden: $this")
			return
		}
		if (anchorNode != null && modelNode != null) {
			anchorNode!!.removeChildNode(modelNode!!)
			Log.d(TAG, "Hiding $this")
		}
	}
	private fun VisualAsset.setVisibility(show : Boolean) {
		if (show)
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
	private var VisualAsset.avgDistance : Float?
		get() = this@SceneviewActivity.averageDistance[this]
		set(value) = this@SceneviewActivity.averageDistance.set(this, value!!)
	private fun VisualAsset.newDistance(d: Float) : Float {
		if (this.avgDistance == null)
			this.avgDistance = d
		else
			this.avgDistance = (this.avgDistance!! * (DISTANCE_ROLLING_AVG_N-1) + d) / DISTANCE_ROLLING_AVG_N
		return this.avgDistance!!
	}

	// Keep track of functions to execute every frame (e.g. checking conditions)
	private val thingsToDo : ArrayList<((Session, Frame) -> Unit)> = ArrayList()

	private fun updateScene() {
		isLoading = true
		clearScene()
		arml.handle()
		isLoading = false
		updateSceneRequested = false
	}

	private fun clearScene() {
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

	private fun ARML.handle() {
		Log.d(TAG, this.toString())

		// Disable any update requested during processing
		updateSceneRequested = false

		// Process ARML
		this.elements.forEach { it.handle() }

		// OnFrameUpdate: Detect and assign anchors to elements in queue
		thingsToDo.add { _, frame ->
			val planes = frame.getUpdatedPlanes()
			assignPlanes(planes)
		}

		// OnFrameUpdate: Check conditions
		thingsToDo.add { _, _ -> conditionalVisualAssets.forEach { it.checkConditions() } }

		// OnFrameUpdate: Refresh scene when requested
		// Only add this at the end. clearScene clears thingsToDo list, so there will be a problem with concurrency
		thingsToDo.add { _, _ ->
			if (updateSceneRequested)
				updateScene()
		}
	}

	private fun ARElement.handle() {
		arElementHandlers.getOrElse(this.arElementType) {
			Log.w(TAG, "Got top level ${this.arElementType}. That type is not supported yet.")
			null
		}?.invoke(this)
	}

	private fun assignPlanes(planes: Collection<Plane>) {
		val types = Plane.Type.entries

		types.forEach { type ->
			val plane = planes.firstOrNull { it.type == type } ?: return@forEach
			//Log.d(TAG, "Found $type plane!")

			// Get Trackables waiting for that type of plane
			val waiting = queuedAnchors[type]!!
			if (waiting.isEmpty())
				return@forEach

			// Create google.Anchor from Plane
			val anchor = plane.createAnchor(plane.centerPose)

			waiting.forEach { trackable ->
				// Create AnchorNode from google.Anchor, and associate it to Anchor (trackable) adding it to the scene
				trackable.addToScene(anchor)
				Log.d(TAG, "Assigned anchor to $trackable")

				// Add RelativeTos referencing this Trackable
				copyAnchorNode(trackable)
			}

			// All processed. Clear queue
			queuedAnchors[type]!!.clear()
		}
	}

	private fun Feature.handle() {
		if (!this.enabled) return
		Log.d(TAG, "Got Feature $this")
		this.anchors.forEach { it.handle() }
	}

	private fun Anchor.handle() {
		if (!this.enabled) return
		anchorHandlers.getOrElse(this.arElementType) {
			Log.w(TAG, "Got a ${this.arElementType} anchor. That type is not supported yet.")
			null
		}?.invoke(this)
	}

	private fun Trackable.handle() {
		if (!this.enabled) return
		Log.d(TAG, "Got Trackable $this")

		this.sortedConfig.forEach {
			val planeType = trackerMap[it.tracker] ?: return@forEach
			queuedAnchors[planeType]!!.putIfAbsent(this)
			Log.d(TAG, "Waiting for anchor for $this")
			return
		}
	}

	private fun RelativeTo.handle() {
		if (!this.enabled) return
		Log.d(TAG, "Got RelativeTo $this")

		if (this.ref == "#user") {
			//TODO
			return
		}

		val other = arml.elementsById[this.ref]
		if (other == null) {
			Log.w(TAG, "RelativeTo $this trying to reference an element that does not exist (yet?).")
			return
		}
		if (other !is RelativeToAble) {
			Log.w(TAG, "RelativeTo $this trying to reference an element that is not supported (${other.arElementType}).")
			return
		}
		relativeToRefHandler.getOrElse(other.arElementType) {
			Log.w(TAG, "Got a RelativeTo referencing a ${other.arElementType}. That type has not been implemented yet.")
			null
		}?.invoke(other, this)
	}

	private fun Trackable.handleRelativeTo(relativeTo: RelativeTo) {
		if (assignedAnchors.containsKey(this)) {
			val otherAnchorNode = this.anchorNode!!
			addRelativeAnchorNode(relativeTo, otherAnchorNode)
			Log.d(TAG, "Created $relativeTo")
		} else {
			queuedRelativeAnchors.putIfAbsent(this, ArrayList())
			queuedRelativeAnchors[this]!!.add(relativeTo)
			Log.d(TAG, "Waiting for anchor for $relativeTo, aka $this")
		}
	}

	private fun RelativeTo.handleRelativeTo(relativeTo: RelativeTo) {
		if (assignedAnchors.containsKey(this)) {
			val otherAnchorNode = this.anchorNode!!
			addRelativeAnchorNode(relativeTo, otherAnchorNode)
			Log.d(TAG, "Created $relativeTo")
		} else {
			queuedRelativeAnchors.putIfAbsent(this, ArrayList())
			queuedRelativeAnchors[this]!!.add(relativeTo)
			Log.d(TAG, "Waiting for anchor for $relativeTo, aka $this")
		}
	}

	private fun Geometry.handleRelativeTo(relativeTo: RelativeTo) {
		if (relativeTo.geometry is LineString) {
			Log.w(TAG, "Got a RelativeTo referencing $this of type LineString. LineStrings are explicitly not supported.")
			return
		}
		Log.w(TAG, "Got a RelativeTo referencing $this of type Geometry. That type is not supported yet.") //TODO
	}

	private fun Trackable.addToScene(anchor: com.google.ar.core.Anchor) {
		val anchorNode = AnchorNode(sceneView.engine, anchor)
		this.setAnchorNode(anchorNode)
		sceneView.addChildNode(anchorNode)
		this.sortedAssets.forEach {
			if (!it.enabled) return@forEach
			assetHandlers.getOrElse(it.arElementType) {
				Log.w(TAG, "Got a ${it.arElementType} asset. That type is not supported yet.")
				null
			}?.invoke(anchorNode, it)
		}
	}

	private fun loadAndAddChildNode(anchorNode: AnchorNode, model: Model) {
		lifecycleScope.launch {
			isLoading = true

			val modelInstance = sceneView.modelLoader.loadModelInstance(model.href)

			modelInstance?.apply {
				//setPriority(7)
				setBlendOrder(model.zOrder ?: 0)
				setGlobalBlendOrderEnabled(true)
			}

			modelInstance?.let {
				val modelNode = ModelNode(
					modelInstance = it,
					scaleToUnits = null,
					centerOrigin = Position(0f,0f,0f)
				)

				modelNode.apply {
					isEditable = true
					this.scale = this.scale.times(model.scaleVector)
					this.rotation = this.rotation.plus(model.rotationVector)

					//setPriority(7)
					setBlendOrder(model.zOrder ?: 0)
					setGlobalBlendOrderEnabled(true)
				}

				addModelToScene(
					anchorNode = anchorNode,
					modelNode = modelNode,
					model = model
				)
			}

			isLoading = false
		}
	}

	private fun addModelToScene(anchorNode: AnchorNode, modelNode: ModelNode, model: Model) {
		model.setAnchorNode(anchorNode)
		model.setModelNode(modelNode)

		//FIXME: Not working
		model.isSelected = false
		modelNode.onDoubleTap = { model.toggleSelected(); true }


		if (model.conditions.isNotEmpty()) {
			conditionalVisualAssets.add(model)
			Log.d(TAG, "Added $model to conditionalVisualAssets")
		}

		if (model.evaluateConditions()) {
			anchorNode.addChildNode(modelNode)  // equivalent to show()
			Log.d(TAG, "Placed $model")
		}
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
		val newPos = relativeTo.geometry.let {
			when(it) {
				is Point -> it.relativeTo(other)
				else -> Position(0f,0f,0f)
			}
		}

		val newAnchorNode = AnchorNode(sceneView.engine, other.anchor).apply {
			isEditable = true
			other.addChildNode(this)
			position = newPos
		}
		relativeTo.setAnchorNode(newAnchorNode)

		relativeTo.assets.forEach {
			assetHandlers.getOrElse(it.arElementType) {
				Log.w(TAG, "Got a ${it.arElementType} asset. That type is not supported yet.")
				null
			}?.invoke(newAnchorNode, it)
		}
	}

	private fun Point.relativeTo(anchorNode: AnchorNode): Position {
		//Let's assume 3 dimensions
		return anchorNode.position.plus(Float3(this.pos[0], this.pos[1], this.pos[2]))
	}

	private fun VisualAsset.checkConditions() {
		this.setVisibility(this.evaluateConditions())
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

	private fun VisualAsset.evaluateSelectedCondition(condition: SelectedCondition): Boolean {
		//TODO: Consider listener property (since gesture detection is not even working, i'll ignore this)
		return this.isSelected == condition.selected
	}

	private fun VisualAsset.evaluateDistanceCondition(condition: DistanceCondition): Boolean {
		val modelPose: Pose = this.anchorNode!!.anchor.pose
		val cameraPose = sceneView.cameraNode.pose

		val distance : Float = cameraPose?.distanceTo(modelPose)?.absoluteValue ?: return false

		/*
		val distance : Float =
			(
				// If cameraPose is found, use distance real distance
				if (cameraPose != null)
					this.newDistance(cameraPose.distanceTo(modelPose))
				// Otherwise, use old distance
				else if (this.avgDistance != null)
					this.avgDistance!!
				// When nothing is available, just return false
				else return false
			)
			.absoluteValue
		 */

		//Log.d(TAG, "Distance: $distance (${this.id})")


		condition.min?.let { if (distance < it) return false }
		condition.max?.let { if (distance > it) return false }


		/*

		// Smooth transitions; Avoid showing and hiding model repeatedly; Like a rubber band

		//	0 ----- Hidden ----- | *(1-DEADZONE) | Min | *(1+DEADZONE) | ----- Shown ----- | ...
		//
		//	- If shown, hide when <*(1-DEADZONE)
		//	- If hidden, show when >*(1+DEADZONE) => keep hidden while <*(1+DEADZONE)
		if (condition.min != null)
			if (distance < (if (!this.isHidden) condition.min!! *(1-DEADZONE) else condition.min!! * (1+DEADZONE)))
				return false

		//	0 ----- Shown ----- | *(1-DEADZONE) | Max | *(1+DEADZONE) | ----- Hidden ----- | ...
		//
		//	- If shown, hide when >*(1+DEADZONE)
		//	- If hidden, show when <*(1-DEADZONE) => keep hidden while >*(1-DEADZONE)
		if (condition.max != null)
			if (distance > (if (!this.isHidden) condition.max!! *(1+DEADZONE) else condition.max!! * (1-DEADZONE)))
				return false

		 */

		return true
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

	// Log filter:
	// package:mine ARML | Analyzer  | QR | (level:error & -tag:Camera & -tag:OpenGLRenderer & -message:static_feature & -message:bundle_adjustment & -message:hit_test & -message:depth & -message:IMU & -message:landmarks & -message:stream)
}
