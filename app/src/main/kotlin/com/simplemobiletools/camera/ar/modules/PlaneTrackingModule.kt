package com.simplemobiletools.camera.ar.modules

import android.util.Log
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.simplemobiletools.camera.ar.SceneController
import com.simplemobiletools.camera.ar.SceneState
import com.simplemobiletools.camera.ar.arml.elements.Trackable
import com.simplemobiletools.camera.ar.putIfAbsent
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import java.util.EnumMap

class PlaneTrackingModule(
	private val sceneView: ARSceneView,
	private val sceneState: SceneState
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
			Config(sceneView.session).apply {
				planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
			}
		)

		isEnabled = true
	}

	override fun disable() {
		if (!isEnabled)
			return

		sceneView.planeRenderer.isEnabled = false
		sceneView.session!!.configure(
			Config(sceneView.session).apply {
				planeFindingMode = Config.PlaneFindingMode.DISABLED
			}
		)

		isEnabled = false
	}

	override fun reset() {
		queuedAnchors.values.forEach { it.clear() }
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
		Log.d(TAG, "Waiting for anchor (Plane) for $trackable")
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

		types.forEach { type ->
			val plane = planes.firstOrNull { it.type == type } ?: return@forEach

			if (plane.trackingState == TrackingState.PAUSED || plane.trackingState == TrackingState.STOPPED)
				return@forEach

			// Get Trackables waiting for that type of plane
			val waiting = getQueuedAnchors(type)
			if (waiting.isEmpty())
				return@forEach

			// Create google.Anchor from Plane
			val anchor = plane.createAnchor(plane.centerPose)

			waiting.forEach { trackable ->
				// Create AnchorNode from google.Anchor, and associate it to Anchor (trackable) adding it to the scene
				sceneState.addToScene(trackable, anchor)
				Log.d(TAG, "Assigned anchor to $trackable")

				trackable.sortedAssets.forEach {
					if (!it.enabled) return@forEach
					context.assetHandlers.getOrElse(it.arElementType) {
						Log.w(TAG, "Got a ${it.arElementType} asset. That type is not supported yet.")
						null
					}?.invoke(sceneState.getAnchorNode(trackable)!!, it)
				}

				// Add RelativeTos referencing this Trackable
				context.copyAnchorNode(trackable)
			}

			// All processed. Clear queue
			clearQueuedAnchors(type)
		}
	}
}
