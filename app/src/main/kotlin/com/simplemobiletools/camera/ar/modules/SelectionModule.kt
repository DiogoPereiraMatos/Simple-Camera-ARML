package com.simplemobiletools.camera.ar.modules

import android.util.Log
import com.simplemobiletools.camera.ar.SceneController
import com.simplemobiletools.camera.ar.arml.elements.*
import io.github.sceneview.ar.ARSceneView

class SelectionModule(
	private val sceneController: SceneController,
	private val sceneView: ARSceneView,
) : ARConditionModule {

	companion object {
		private const val TAG = "SELECTION_MODULE"
	}

	// SelectedCondition: Keep track of what assets are selected
	private val isSelected : HashMap<VisualAsset, Boolean> = HashMap()

	private var isEnabled = false
	override fun isEnabled(): Boolean = isEnabled

	override fun enable() {
		if (isEnabled)
			return

		/*
		sceneView.setOnGestureListener(
			onDoubleTap = { event, node ->
				Log.d(TAG, "Double tapped ( ${event.x} , ${event.y} ). Node: $node. Hit: ${sceneView.hitTestAR().toString()}")
				node?.onDoubleTap(event)
			}
		)
		 */

		isEnabled = true
	}

	override fun disable() {
		if (!isEnabled)
			return

		sceneView.setOnGestureListener(
			onSingleTapUp = { _, _ -> ; },
		)

		isEnabled = false
	}

	override fun reset() {
		isSelected.clear()
	}





	fun isSelected(visualAsset: VisualAsset): Boolean {
		return isSelected[visualAsset] ?: false
	}

	fun isSelected(anchor: Anchor): Boolean {
		val assets = when (anchor) {
			is ARAnchor -> anchor.assets
			is ScreenAnchor -> anchor.assets
			else -> {
				Log.e(TAG, "Unexpected anchor type: $anchor")
				return false
			}
		}
		return assets.any { isSelected(it) }
	}

	fun isSelected(feature: Feature): Boolean {
		return feature.anchors.any { isSelected(it) }
	}

	fun setSelected(visualAsset: VisualAsset, selected: Boolean) {
		isSelected[visualAsset] = selected
		Log.d(TAG, "${if (selected) "Selected" else "Unselected" } ${visualAsset.toShortString()}")
	}

	fun select(visualAsset: VisualAsset) {
		setSelected(visualAsset, true)
	}

	fun unselect(visualAsset: VisualAsset) {
		setSelected(visualAsset, false)
	}

	fun toggleSelected(visualAsset: VisualAsset): Boolean {
		setSelected(visualAsset, !isSelected(visualAsset))
		return isSelected(visualAsset)
	}

	override fun evaluateCondition(visualAsset: VisualAsset, condition: Condition): Boolean {
		condition as SelectedCondition

		return when (condition.listener) {
			Listener.FEATURE -> isSelected(sceneController.getFeature(visualAsset)) == condition.selected
			Listener.ANCHOR -> isSelected(sceneController.getAnchor(visualAsset)) == condition.selected
		}
	}
}
