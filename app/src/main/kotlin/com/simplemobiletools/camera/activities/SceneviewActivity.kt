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
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.arcore.position
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.node.PoseNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch


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
			planeRenderer.isEnabled = true
			configureSession { session, config ->
				config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
					true -> Config.DepthMode.AUTOMATIC
					else -> Config.DepthMode.DISABLED
				}
				config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
				config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
			}
			onSessionUpdated = { session, frame -> thingsToDo.forEach {it(session, frame)} }
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
							
								<Feature id="centerFeature">
									<anchors>
										<Trackable id="centreTrackable">
											<config>
												<tracker xlink:href="#genericPlaneTracker" />
												<src>look for a plane, idk</src>
											</config>
											<assets>
												<Model id="myModel">
													<href xlink:href="$virtualObjectName" /> 
												</Model>
											</assets>
										</Trackable>
									</anchors>
								</Feature>
							
								<Feature id="orbitFeature">
									<anchors>
										<RelativeTo>
											<ref xlink:href="centreTrackable" />
											<gml:Point gml:id="marsOffset" srsDimension="3">
												<gml:pos>
													0 0 2.5
												</gml:pos>
											</gml:Point>
											<assets>
												<Model id="marsModel">
													<href xlink:href="models/Mars/Mars.gltf" /> 
												</Model>
											</assets>
										</RelativeTo>
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

	private val thingsToDo : ArrayList<((Session, Frame) -> Unit)> = ArrayList()

	// Trackables go in here vvv
	private val queuedAnchors : ArrayList<Trackable> = ArrayList()

	// RelativeTo waiting for trackables go in here vvv
	private val queuedRelativeAnchors : HashMap<com.simplemobiletools.camera.ar.arml.elements.Anchor, RelativeTo> = HashMap()

	// Assigned Trackables and RelativeTo go in here vvv
 	private val assignedAnchors : HashMap<com.simplemobiletools.camera.ar.arml.elements.Anchor, AnchorNode> = HashMap()

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
		+-------------+   +-------------+   More RelativeTo?
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
			}
		}

		// disable any update requested during processing
		updateSceneRequested = false
		
		// Only add this at the end of processing
		thingsToDo.add { _, frame ->
			if (updateSceneRequested) {
				isLoading = true
				clearScene()
				handleARML()
				isLoading = false
				updateSceneRequested = false
			}
			frame.getUpdatedPlanes()
				.firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
				?.let { plane ->
					queuedAnchors.forEach {
						Log.d(TAG, "Assigned anchor to $it")
						addAnchorNodeToScene(plane.createAnchor(plane.centerPose), it)

						// Relative
						//TODO: Queued relative anchors can reference other queued relative anchors, so do this recursively? Or check entire list in the end
						if (queuedRelativeAnchors.containsKey(it)) {
							val relativeTo = queuedRelativeAnchors[it]!!
							val otherAnchorNode = assignedAnchors[it]!!
							addRelativeAnchorNode(relativeTo, otherAnchorNode)
							Log.d(TAG, "Created $relativeTo")
							queuedRelativeAnchors.remove(it)
						}
					}
					queuedAnchors.clear()
				}
		}
	}

	private fun handleFeature(feature: Feature) {
		Log.d(TAG, "Got Feature $feature")
		if (feature.enabled == false) return
		val anchors = feature.anchors

		anchors.forEach {
			when(it) {
				is Trackable -> handleTrackable(it)
				is RelativeTo -> handleRelativeTo(it)
			}
		}
	}

	private fun handleTrackable(trackable: Trackable) {
		Log.d(TAG, "Got Trackable $trackable")

		if (trackable.config.any { it.tracker == "#genericPlaneTracker" }) {
			if (!assignedAnchors.containsKey(trackable))
				if (!queuedAnchors.contains(trackable)) {
					queuedAnchors.add(trackable)
					Log.d(TAG, "Waiting for anchor for $trackable")
				}
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
					queuedRelativeAnchors[other] = relativeTo
					Log.d(TAG, "Waiting for anchor for $relativeTo, aka $other")
				}
			}
			else -> Log.e(TAG, "Got a RelativeTo referencing $other. Only Trackables are accepted.")
		}
	}

	private fun addRelativeAnchorNode(relativeTo: RelativeTo, other: AnchorNode) {
		val sceneView = binding.sceneView

		val otherPos = other.position
		var newPos = Position(0f,0f,0f)
		when(val geometry = relativeTo.geometry) {
			is Point -> {
				// TODO: Do this in an higher order Point
				val coords = geometry.pos
					.split(" ")
					.map(String::toFloat)
				newPos = otherPos.plus(Float3(coords[0], coords[1], coords[2]))
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

		val virtualObjectName = model.href
		val scale = minOf(
			model.scale?.x ?: 1.0,
			model.scale?.y ?: 1.0,
			model.scale?.z ?: 1.0
		)

		anchor.apply {
			isEditable = true
			lifecycleScope.launch {
				isLoading = true
				sceneView.modelLoader.loadModelInstance(
					virtualObjectName
				)?.let { modelInstance ->
					val modelNode = ModelNode(
						modelInstance = modelInstance,
						// Scale to fit in a _scale_ meters cube
						scaleToUnits = scale.toFloat(),
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
	}
}
