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
import com.simplemobiletools.camera.databinding.ActivitySceneviewBinding
import com.simplemobiletools.camera.extensions.config
import com.simplemobiletools.camera.extensions.listFilesInDirectory
import com.simplemobiletools.commons.extensions.viewBinding


class SceneviewActivity : SimpleActivity() {

	// Log filter:
	// package:mine tag~:^CameraX.* | tag~:^QR.* | tag~:ARML | tag~:SCENE.* | tag~:.*MODULE | (level:error & -message:motion_tracking_context & -message:static_feature_frame_selector & -message:hit_test & -message:vio_initializer)

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

	private var selectedARMLPath : String = "armlexamples/empty.xml"
	private lateinit var sceneController: SceneController


	override fun onCreate(savedInstanceState: Bundle?) {
		useDynamicTheme = false
		super.onCreate(savedInstanceState)
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		supportActionBar?.hide()

		setContentView(binding.root)

		// Setup
		setupWindow()
		setupButtons()

		sceneController = SceneController(this, sceneView)
		intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
			sceneController.setARMLFromPath(it)
		}
	}

	override fun onResume() {
		super.onResume()

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
		val strings : Array<String> = projectAssets.listFilesInDirectory("armlexamples")?.toTypedArray() ?: Array(0) {null.toString()}

		AlertDialog.Builder(this)
			.setTitle("ARML")
			.setSingleChoiceItems(strings, strings.indexOf(selectedARMLPath)) { _, which ->
				run {
					sceneController.setARMLFromPath(strings[which])
					selectedARMLPath = strings[which]
				}
			}
			.show()
	}

	private fun launchSettings() {
		Intent(this, SettingsActivity::class.java).also {
			startActivity(it)
		}
	}
}
