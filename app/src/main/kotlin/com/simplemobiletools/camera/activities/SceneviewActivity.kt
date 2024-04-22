package com.simplemobiletools.camera.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import com.simplemobiletools.camera.R
import com.simplemobiletools.camera.ar.arml.ARMLParser
import com.simplemobiletools.camera.ar.arml.elements.ARElement
import com.simplemobiletools.camera.ar.arml.elements.ARML
import com.simplemobiletools.camera.ar.arml.elements.Feature
import com.simplemobiletools.camera.ar.arml.elements.ScreenAnchor
import com.simplemobiletools.camera.databinding.ActivitySceneviewBinding
import com.simplemobiletools.camera.extensions.config
import com.simplemobiletools.commons.extensions.navigationBarHeight
import com.simplemobiletools.commons.extensions.viewBinding
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch
import java.util.ArrayList


class SceneviewActivity : SimpleActivity() {

	companion object val TAG = "ARML"

	private val binding by viewBinding(ActivitySceneviewBinding::inflate)

	val arml : ARML = ARMLParser().loads(
		"""
			<arml xmlns="http://www.opengis.net/arml/2.0" 
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> 
				<ARElements>
				</ARElements>
			</arml>
		""".trimIndent()
	) ?: ARML()

	var virtualObjectName : String = "models/damaged_helmet.glb"
		set(value) {
			if (field != value)
				updateSceneRequested = true
				Log.d(TAG, "Set $value")
			field = value
		}
	var updateSceneRequested : Boolean = false

	var isLoading = false
		set(value) {
			field = value
			binding.loadingView.isGone = !value
		}

	var anchorNode: AnchorNode? = null
		set(value) {
			if (field != value) {
				field = value
				updateInstructions()
			}
		}

	var trackingFailureReason: TrackingFailureReason? = null
		set(value) {
			if (field != value) {
				field = value
				updateInstructions()
			}
		}

	private fun updateInstructions() {
		binding.instructionText.text = trackingFailureReason?.getDescription(this) ?: if (anchorNode == null) {
			"Point your phone down..."
		} else {
			null
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		useDynamicTheme = false
		super.onCreate(savedInstanceState)
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		supportActionBar?.hide()

		setContentView(binding.root)

		// A lot of flags here...
		// Some of the code is copied from MainActivity
		// Some is from StackOverflow, to render over notch (https://stackoverflow.com/questions/49190381/fullscreen-app-with-displaycutout)
		// Don't be surprised if there are redundant or useless flags
		// It works

		window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
		window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
		//window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

		WindowCompat.setDecorFitsSystemWindows(window, false)
		ViewCompat.setOnApplyWindowInsetsListener(binding.rootView) { _, windowInsets ->
			val safeInsetBottom = windowInsets.displayCutout?.safeInsetBottom ?: 0
			val safeInsetTop = windowInsets.displayCutout?.safeInsetTop ?: 0

			binding.settings.updateLayoutParams<ViewGroup.MarginLayoutParams> {
				topMargin = safeInsetTop
			}

			binding.arSettingsButton.updateLayoutParams<ViewGroup.MarginLayoutParams> {
				topMargin = safeInsetTop
			}

			val marginBottom = safeInsetBottom + navigationBarHeight + resources.getDimensionPixelSize(com.simplemobiletools.commons.R.dimen.bigger_margin)

			WindowInsetsCompat.CONSUMED
		}

		val windowInsetsController = ViewCompat.getWindowInsetsController(window.decorView)
		windowInsetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
		windowInsetsController?.hide(WindowInsetsCompat.Type.statusBars() /*or WindowInsetsCompat.Type.navigationBars()*/)

		binding.apply {
			setupSceneView()
			setupButtons()
		}
	}

	override fun onResume() {
		super.onResume()
		if (!this.config.isArmlEnabled)
			this.finish()

		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		ensureTransparentNavigationBar()

		if (ViewCompat.getWindowInsetsController(window.decorView) == null) {
			window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		}
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
				config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
				config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
			}
			onSessionUpdated = { _, frame ->
				if (anchorNode == null) {
					frame.getUpdatedPlanes()
						.firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
						?.let { plane ->
							addAnchorNode(plane.createAnchor(plane.centerPose))
						}
				} else {
					if (updateSceneRequested) {
						anchorNode?.clearChildNodes()
						anchorNode?.apply { loadAndAddChildNode(this) }
						updateSceneRequested = false
					}
				}
			}
			onTrackingFailureChanged = { reason ->
				this@SceneviewActivity.trackingFailureReason = reason
			}
		}

		// ARML
		sceneView.apply {
			val arElements : List<ARElement> = arml.elements.elements

			for (element in arElements)
			{
				if (element is Feature)
				{
					continue
				}

				if (element is ScreenAnchor)
				{
					element.assets.labels
				}
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
						R.id.model_settings -> launchModelSettingsMenuDialog()
						else -> null
					} != null
				}
				inflate(R.menu.model_settings_menu)
				show()
			}
		}
	}

	private fun listAssets(path: String): ArrayList<String>? {

		val file_or_folder = assets.list(path)!!
		if (file_or_folder.isEmpty())
			return null

		val all_assets = ArrayList<String>()
		for (f in file_or_folder) {
			val recursive = listAssets("$path/$f")
			if (recursive != null) {
				// is folder
				all_assets.addAll(recursive)
			} else {
				// is file
				all_assets.add("$path/$f")
			}
		}
		return all_assets
	}

	private fun launchModelSettingsMenuDialog() {
		val models = listAssets("models")!!.filter { filename ->
			filename.endsWith(".glb", true)
				||
				filename.endsWith(".gltf", true)
		}.toTypedArray()

		AlertDialog.Builder(this)
			.setTitle("Model")
			.setSingleChoiceItems(models, models.indexOf(virtualObjectName)) { _, which -> virtualObjectName = models[which] }
			.show()
	}

	fun addAnchorNode(anchor: Anchor) {
		val sceneView = binding.sceneView
		sceneView.addChildNode(
			AnchorNode(sceneView.engine, anchor)
				.apply {
					loadAndAddChildNode(this)
					anchorNode = this
				}
		)
	}

	fun loadAndAddChildNode(anchor: AnchorNode) {
		val sceneView = binding.sceneView
		anchor.apply {
			isEditable = true
			lifecycleScope.launch {
				isLoading = true
				sceneView.modelLoader.loadModelInstance(
					virtualObjectName
				)?.let { modelInstance ->
					val modelNode = ModelNode(
						modelInstance = modelInstance,
						// Scale to fit in a 0.5 meters cube
						scaleToUnits = 0.5f,
						// Bottom origin instead of center so the model base is on floor
						centerOrigin = Position(y = -0.5f)
					).apply {
						isEditable = true
					}
					addChildNode(modelNode)
				}
				isLoading = false
			}
		}
	}

	fun launchSettings() {
		val intent = Intent(this, SettingsActivity::class.java)
		startActivity(intent)
	}
}
