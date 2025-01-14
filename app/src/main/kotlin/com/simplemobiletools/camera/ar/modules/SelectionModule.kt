package com.simplemobiletools.camera.ar.modules

import android.util.Log
import com.simplemobiletools.camera.ar.SceneController
import com.simplemobiletools.camera.ar.arml.elements.Condition
import com.simplemobiletools.camera.ar.arml.elements.SelectedCondition
import com.simplemobiletools.camera.ar.arml.elements.VisualAsset
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
			onDoubleTap = { _, _ -> ; }
		)

		isEnabled = false
	}

	override fun reset() {
		isSelected.clear()
	}





	fun isSelected(visualAsset: VisualAsset): Boolean {
		return isSelected[visualAsset] ?: false
	}

	fun setSelected(visualAsset: VisualAsset, selected: Boolean) {
		isSelected[visualAsset] = selected
		Log.d(TAG, "${if (selected) "Selected" else "Unselected" } VisualAsset(id=${visualAsset.id})")
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

		//TODO: Consider listener property (since gesture detection is not even working, i'll ignore this)
		return isSelected(visualAsset) == condition.selected
	}
}
