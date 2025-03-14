package com.simplemobiletools.camera.ar.modules

import android.util.Log
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.simplemobiletools.camera.ar.SceneController
import com.simplemobiletools.camera.ar.arml.elements.Trackable
import com.simplemobiletools.camera.extensions.putIfAbsent
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Size
import java.util.EnumMap

class PlaneTrackingModule(
	private val sceneController: SceneController,
	private val sceneView: ARSceneView,
) : ARTrackingModule {

	companion object {
		private const val TAG = "PLANE_TRACKING_MODULE"
	}

	// Trackables waiting for an AnchorNode (Plane) go in here vvv
	private val queuedAnchors : EnumMap<Plane.Type, ArrayList<Trackable>> = EnumMap(Plane.Type.entries.associateWith { ArrayList() })

	private var isEnabled = false
	override fun isEnabled(): Boolean = isEnabled

	override fun enable() {
		if (isEnabled)
			return

		sceneView.planeRenderer.isEnabled = true
		sceneView.session!!.configure(
			sceneView.session!!.config.apply {
				planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
			}
		)

		isEnabled = true
		Log.d(TAG, "Enabled PlaneTracking.")
	}

	override fun disable() {
		if (!isEnabled)
			return

		sceneView.planeRenderer.isEnabled = false
		sceneView.session!!.configure(
			sceneView.session!!.config.apply {
				planeFindingMode = Config.PlaneFindingMode.DISABLED
			}
		)

		isEnabled = false
		Log.d(TAG, "Disabled PlaneTracking.")
	}

	override fun reset() {
		queuedAnchors.values.forEach { it.clear() }
		Log.d(TAG, "Reset PlaneTracking.")
	}

	override fun onFrameUpdate(context: SceneController, frame: Frame) {
		if (!isEnabled)
			return

		// OnFrameUpdate: Detect and assign anchors to elements in image queue
		val planes = frame.getUpdatedPlanes()
		assignPlanes(context, planes)
	}







	fun addToPlaneQueue(trackable: Trackable, planeType: Plane.Type) {
		queuedAnchors[planeType]!!.putIfAbsent(trackable)
		Log.d(TAG, "Waiting for anchor (Plane) for ${trackable.toShortString()}")
		return
	}

	fun getQueuedAnchors(planeType: Plane.Type): ArrayList<Trackable> {
		return queuedAnchors[planeType]!!
	}

	fun clearQueuedAnchors(planeType: Plane.Type) {
		queuedAnchors[planeType]!!.clear()
	}

	private fun assignPlanes(context: SceneController, planes: Collection<Plane>) {
		val types = Plane.Type.entries

		for (type in types) {
			val plane = planes.firstOrNull { it.type == type } ?: continue

			if (plane.trackingState == TrackingState.PAUSED || plane.trackingState == TrackingState.STOPPED)
				continue

			// Get Trackables waiting for that type of plane
			val waiting = getQueuedAnchors(type)
			if (waiting.isEmpty())
				continue

			Log.d(TAG, "Detected Plane: type=${plane.type}; tracking=${plane.trackingState}; size=(${plane.extentX},${plane.extentZ})")

			// Create google.Anchor from Plane
			val anchor = plane.createAnchor(plane.centerPose)
			//TODO: Save extentX and extentY

			for (trackable in waiting) {

				// Create AnchorNode from google.Anchor, and associate it to Anchor (trackable), adding it to the scene
				val anchorNode = AnchorNode(sceneView.engine, anchor).apply {
					transform(
						position = Position(0f, 0f, 0f),
						rotation = Rotation(0f, 0f, 0f),
						scale = Size(1f, 1f, 1f)
					)
				}
				sceneController.setParentNode(trackable, anchorNode)
				sceneView.addChildNode(anchorNode)
				Log.d(TAG, "Assigned anchor to ${trackable.toShortString()}")

				//TODO: Preload assets
				for (asset in trackable.sortedAssets) {
					if (!asset.enabled) continue

					// Call handler
					context.assetHandlers.getOrElse(asset.arElementType) {
						Log.w(TAG, "Got a ${asset.arElementType} asset. That type is not supported yet. Ignoring...")
						null
					}?.invoke(sceneController.getParentNode(trackable)!!, asset)
				}

				// Add RelativeTos referencing this Trackable
				context.propagateNode(trackable)
			}

			// All processed. Clear queue
			clearQueuedAnchors(type)
		}
	}
}
