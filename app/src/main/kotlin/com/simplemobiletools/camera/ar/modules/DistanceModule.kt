package com.simplemobiletools.camera.ar.modules

import android.util.Log
import com.simplemobiletools.camera.ar.SceneController
import com.simplemobiletools.camera.ar.arml.elements.Condition
import com.simplemobiletools.camera.ar.arml.elements.DistanceCondition
import com.simplemobiletools.camera.ar.arml.elements.VisualAsset
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.position
import io.github.sceneview.math.Position

class DistanceModule(
	private val sceneController: SceneController,
	private val sceneView: ARSceneView,
) : ARConditionModule {

	companion object {
		private const val TAG = "DISTANCE_MODULE"
		const val DISTANCE_ROLLING_AVG_N = 3
		const val DEADZONE = 0.1 //%
	}

	// DistanceCondition: Keep track of the rolling average distance of the asset
	private val averageDistance : HashMap<VisualAsset, Float> = HashMap()

	private var isEnabled = false
	override fun isEnabled(): Boolean = isEnabled

	override fun enable() {
		if (isEnabled)
			return
		isEnabled = true
	}

	override fun disable() {
		if (!isEnabled)
			return
		isEnabled = false
	}

	override fun reset() {
		averageDistance.clear()
	}




	fun getAverageDistance(visualAsset: VisualAsset): Float? {
		return averageDistance[visualAsset]
	}

	fun setAverageDistance(visualAsset: VisualAsset, averageDistance : Float) {
		this.averageDistance[visualAsset] = averageDistance
	}

	fun clearAverageDistance(visualAsset: VisualAsset) {
		this.averageDistance.remove(visualAsset)
	}

	fun newDistance(visualAsset: VisualAsset, d: Float) : Float {
		when (val averageDistance = getAverageDistance(visualAsset)) {
			null -> d
			else -> (averageDistance * (DISTANCE_ROLLING_AVG_N - 1) + d) / DISTANCE_ROLLING_AVG_N
		}.let {
			setAverageDistance(visualAsset, it)
			return it
		}
	}

	private fun Position.distanceTo(position: Position): Float {
		return kotlin.math.sqrt(
			(this.x - position.x) * (this.x - position.x) +
					(this.y - position.y) * (this.y - position.y)
		)
	}

	override fun evaluateCondition(visualAsset: VisualAsset, condition: Condition): Boolean {
		condition as DistanceCondition

		if (!sceneController.hasParentNode(visualAsset)) {
			Log.e(TAG, "No parent node for ${visualAsset.toShortString()}")
			return false
		}

		val modelPosition = sceneController.getParentNode(visualAsset)!!.position
		val cameraPosition = sceneView.cameraNode.pose?.position ?: return false

		val distance = modelPosition.distanceTo(cameraPosition)
		//Log.d(TAG, "Distance: $distance (${visualAsset.id})")

		condition.min?.let { if (distance < it) return false }
		condition.max?.let { if (distance > it) return false }


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
}
