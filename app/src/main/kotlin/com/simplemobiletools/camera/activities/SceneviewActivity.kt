package com.simplemobiletools.camera.activities

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.*
import com.simplemobiletools.camera.R
import com.simplemobiletools.camera.ar.SceneController
import com.simplemobiletools.camera.ar.SceneState
import com.simplemobiletools.camera.ar.listAssets
import com.simplemobiletools.camera.databinding.ActivitySceneviewBinding
import com.simplemobiletools.camera.extensions.config
import com.simplemobiletools.commons.extensions.viewBinding


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

class SceneviewActivity : SimpleActivity() {

	// Log filter:
	// package:mine tag:SCENE | tag:FEATURE | (level:error & -message:motion_tracking_context & -message:static_feature_frame_selector & -message:hit_test & -message:vio_initializer)

	companion object {
		const val TAG = "SCENE_ACTIVITY"
	}

	val binding by viewBinding(ActivitySceneviewBinding::inflate)
	private val sceneView
		get() = binding.sceneView
	private val projectAssets
		get() = assets
	private val projectConfig
		get() = this.config

	private val sceneState = SceneState(sceneView)
	private val sceneController = SceneController(this, sceneView, sceneState)


	override fun onCreate(savedInstanceState: Bundle?) {
		useDynamicTheme = false
		super.onCreate(savedInstanceState)
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		supportActionBar?.hide()

		setContentView(binding.root)

		// Setup
		setupWindow()
		setupButtons()

		sceneController.armlPath = intent.getStringExtra(Intent.EXTRA_TEXT) ?: sceneController.armlPath
		sceneController.setupSceneView()
	}

	override fun onResume() {
		super.onResume()
		if (!this.projectConfig.isArmlEnabled)
			this.finish()

		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)

		sceneController.run()
	}

	override fun onPause() {
		super.onPause()
		window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

		sceneController.stop()
	}



	//=== UI ===//

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

	private fun setupButtons() {
		binding.settings.setOnClickListener {
			launchSettings()
		}
		binding.arSettingsButton.setOnClickListener { v ->
			PopupMenu(this, v).apply {
				setOnMenuItemClickListener { item ->
					when (item.itemId) {
						R.id.refresh_scene -> sceneController.requestSceneUpdate()
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
		val strings = projectAssets.listAssets("armlexamples")!!
			.toTypedArray()

		AlertDialog.Builder(this)
			.setTitle("ARML")
			.setSingleChoiceItems(strings, strings.indexOf(sceneController.armlPath)) { _, which -> sceneController.armlPath = strings[which] }
			.show()
	}

	private fun launchSettings() {
		Intent(this, SettingsActivity::class.java).also {
			startActivity(it)
		}
	}
}
