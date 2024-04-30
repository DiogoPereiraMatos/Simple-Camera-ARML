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
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.simplemobiletools.camera.R
import com.simplemobiletools.camera.ar.arml.ARMLParser
import com.simplemobiletools.camera.ar.arml.elements.*
import com.simplemobiletools.camera.databinding.ActivitySceneviewBinding
import com.simplemobiletools.camera.extensions.config
import com.simplemobiletools.commons.extensions.viewBinding
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch
import java.util.ArrayList


class SceneviewActivity : SimpleActivity() {

	companion object val TAG = "ARML"

	private val binding by viewBinding(ActivitySceneviewBinding::inflate)

	var virtualObjectName : String = "models/damaged_helmet.glb"
		set(value) {
			if (field != value) {
				Log.d(TAG, "Set $value")
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
				config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
				config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
			}
			sceneView.onSessionUpdated = { _, frame ->
				if (updateSceneRequested) {
					clearScene()
					handleARML(arml)
					updateSceneRequested = false
				}
				frame.getUpdatedPlanes()
					.firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
					?.let { plane ->
						queuedAnchors.forEach { addAnchorNode(plane.createAnchor(plane.centerPose), it) }
						queuedAnchors.clear()
					}
			}
		}

		// ARML
		handleARML(arml)
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
			.setSingleChoiceItems(models, models.indexOf(virtualObjectName)) { _, which -> virtualObjectName = models[which] }
			.show()
	}

	fun launchSettings() {
		val intent = Intent(this, SettingsActivity::class.java)
		startActivity(intent)
	}


	//=== ARML ===//

	private val header : String = """xmlns="http://www.opengis.net/arml/2.0" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:xlink="http://www.w3.org/1999/xlink""""

	val arml : ARML
	get() {
		try {
			val result : ARML = ARMLParser().loads(
				"""
					<arml $header> 
						<ARElements>
						
							<Feature id="myFeature">
								<name>$virtualObjectName</name>
								<anchors>
									<Trackable id="planeTrackable">
										<config>
											<tracker xlink:href="#genericPlaneTracker" />
											<src>idk</src>
										</config>
										<assets>
											<Model id="myModel">
												<href xlink:href="$virtualObjectName" /> 
											</Model>
											<Model id="earthModel">
												<href xlink:href="models/Earth/Earth.gltf" /> 
											</Model>
										</assets>
									</Trackable>
								</anchors>
							</Feature>
							
						</ARElements>
					</arml>
				""".trimIndent()
			)

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

	val queuedAnchors : ArrayList<Trackable> = ArrayList()
	val assignedAnchors : HashMap<Trackable, AnchorNode> = HashMap()

	fun handleARML(arml: ARML) {
		Log.d(TAG, arml.toString())

		val arElements : List<ARElement> = arml.elements.elements

		for (element in arElements) {
			if (element is Feature) return handleFeature(element)
		}
	}

	fun handleFeature(feature: Feature) {
		if (feature.enabled == false) return
		val anchors : List<com.simplemobiletools.camera.ar.arml.elements.Anchor> = feature.anchors?.anchors ?: return

		for (anchor in anchors) {
			if (anchor is Trackable) return handleTrackable(anchor)
		}
	}

	fun handleTrackable(trackable: Trackable) {
		// Force update
		updateSceneRequested = true

		if (trackable.config.any { it.tracker.href == "#genericPlaneTracker" }) {
			if (!assignedAnchors.containsKey(trackable))
				if (!queuedAnchors.contains(trackable))
					queuedAnchors.add(trackable)
		}
	}

	fun addAnchorNode(anchor: Anchor, trackable: Trackable) {
		val sceneView = binding.sceneView
		sceneView.addChildNode(
			AnchorNode(sceneView.engine, anchor)
				.apply {
					trackable.assets.assets.forEach {
						if (it is Model)
							loadAndAddChildNode(this, it.href.href)
					}
					assignedAnchors[trackable] = this
				}
		)
	}

	fun loadAndAddChildNode(anchor: AnchorNode, virtualObjectName: String) {
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

	fun clearScene() {
		Log.d(TAG, "Clearing scene...")
		val sceneView = binding.sceneView
		queuedAnchors.clear()
		assignedAnchors.values.forEach {
			it.clearChildNodes()
		}
		assignedAnchors.clear()
		sceneView.clearChildNodes()
	}
}
