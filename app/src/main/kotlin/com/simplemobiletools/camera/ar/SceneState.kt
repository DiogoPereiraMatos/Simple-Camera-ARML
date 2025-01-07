package com.simplemobiletools.camera.ar

import android.graphics.BitmapFactory
import android.util.Log
import com.simplemobiletools.camera.ar.arml.elements.*
import com.simplemobiletools.camera.ar.arml.elements.gml.Point
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.model.setBlendOrder
import io.github.sceneview.model.setGlobalBlendOrderEnabled
import io.github.sceneview.node.ImageNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import kotlinx.coroutines.runBlocking

class SceneState(
	private val sceneView: ARSceneView
) {
	/*
	          ARML        |      SceneView
		+-------------+   |   +-------------+
		|     ARML    |   |   |    Scene    |
		+-------------+   |   +-------------+
		       |          |          |
		       v          |          |
       (Anchor as in:     |          |
        - Trackable       |          |
        - ScreenAnchor    |          |           (tracked by arcore)         (detected by arcore)
        - ...)		      |          v       (com.google.ar.core.Anchor)  (com.google.ar.core.Trackable)
		+-------------+   |   +-------------+     +-------------+          +----------------------+
		|   Anchor    |   |   | AnchorNode  | <-- |    Anchor   | <------- | Trackable (eg Plane) |
		+-------------+   |   +-------------+     +-------------+          +----------------------+
		       |          |          |
		       |          |          | (if placed / shown)
		       v          |          v
		+-------------+   |   +-----------------------+
		|   V.Asset   |   |   |  ModelNode/ImageNode  | (VisualAssetNode)
		+-------------+   |   +-----------------------+
	 */

	// Save AnchorNodes of assigned Trackables and RelativeTo
	private val assignedAnchors : HashMap<Anchor, AnchorNode> = HashMap()

	// Save AnchorNodes of VisualAssets
	private val anchorNodes : HashMap<VisualAsset, AnchorNode> = HashMap()

	// Save ModelNodes and ImageNodes of VisualAssets
	private val visualAssetNodes : HashMap<VisualAsset, Node> = HashMap()

	// RelativeTo waiting for Anchors go in here vvv
	private val queuedRelativeAnchors : HashMap<Anchor, ArrayList<RelativeTo>> = HashMap()

	// Save VisualAssets that have conditions
	val conditionalVisualAssets : ArrayList<VisualAsset> = ArrayList()



	companion object {
		private const val TAG = "SCENE_STATE"
	}

	private val projectAssets = sceneView.context.assets


	fun reset() {
		// Clear queues
		queuedRelativeAnchors.clear()

		// Clear scene nodes
		assignedAnchors.values.forEach { it.clearChildNodes() }
		assignedAnchors.clear()
		sceneView.clearChildNodes()

		// Clear auxiliary stuff
		conditionalVisualAssets.clear()
		visualAssetNodes.clear()
		anchorNodes.clear()
	}



	fun hasAnchorNode(anchor: Anchor): Boolean {
		return assignedAnchors.containsKey(anchor)
	}

	fun getAnchorNode(anchor: Anchor): AnchorNode? {
		return assignedAnchors[anchor]
	}

	fun setAnchorNode(anchor: Anchor, anchorNode: AnchorNode) {
		assignedAnchors[anchor] = anchorNode
	}

	fun hasAnchorNode(visualAsset: VisualAsset): Boolean {
		return anchorNodes.containsKey(visualAsset)
	}

	fun getAnchorNode(visualAsset: VisualAsset): AnchorNode? {
		return anchorNodes[visualAsset]
	}

	fun setAnchorNode(visualAsset: VisualAsset, anchorNode: AnchorNode) {
		anchorNodes[visualAsset] = anchorNode
	}

	fun getVisualAssetNode(visualAsset: VisualAsset): Node? {
		return visualAssetNodes[visualAsset]
	}

	fun setVisualAssetNode(visualAsset: VisualAsset, visualAssetNode: Node) {
		visualAssetNodes[visualAsset] = visualAssetNode
	}




	// Asset is placed only when the ModelNode is a child of the AnchorNode
	// There is no function (that I know of) that hides and shows model on command, so this is how I'll do it...

	fun getVisibility(visualAsset: VisualAsset): Boolean {
		val anchorNode = getAnchorNode(visualAsset)
		val visualAssetNode = getVisualAssetNode(visualAsset)

		return when (anchorNode == null || visualAssetNode == null) {
			true -> false
			false -> anchorNode.childNodes.contains(visualAssetNode)
		}
	}

	fun setVisibility(visualAsset: VisualAsset, visible: Boolean) {
		when (visible) {
			true -> show(visualAsset)
			false -> hide(visualAsset)
		}
	}

	fun show(visualAsset: VisualAsset) {
		if (getVisibility(visualAsset)) {
			return
		}
		val anchorNode = getAnchorNode(visualAsset)
		val visualAssetNode = getVisualAssetNode(visualAsset)

		if (anchorNode == null || visualAssetNode == null) {
			Log.e(TAG, "Error showing asset: $visualAsset")
			return
		}

		Log.d(TAG, "Showing $visualAsset")
		anchorNode.addChildNode(visualAssetNode)
	}

	fun hide(visualAsset: VisualAsset) {
		if (!getVisibility(visualAsset)) {
			return
		}

		val anchorNode = getAnchorNode(visualAsset)
		val visualAssetNode = getVisualAssetNode(visualAsset)

		if (anchorNode == null || visualAssetNode == null) {
			Log.e(TAG, "Error hiding asset: $visualAsset")
			return
		}

		Log.d(TAG, "Hiding $visualAsset")
		anchorNode.removeChildNode(visualAssetNode)
	}







	fun addToRelativeQueue(original: Trackable, new: RelativeTo) {
		queuedRelativeAnchors.putIfAbsent(original, ArrayList())
		queuedRelativeAnchors[original]!!.add(new)
		Log.d(TAG, "Waiting for anchor for $new, aka $original")
	}

	fun addToRelativeQueue(original: RelativeTo, new: RelativeTo) {
		queuedRelativeAnchors.putIfAbsent(original, ArrayList())
		queuedRelativeAnchors[original]!!.add(new)
		Log.d(TAG, "Waiting for anchor for $new, aka $original")
	}

	fun getWaitingFor(anchor: Anchor): ArrayList<RelativeTo>? {
		return queuedRelativeAnchors[anchor]
	}

	fun clearQueuedRelativeAnchors(anchor: Anchor) {
		queuedRelativeAnchors.remove(anchor)
	}

	fun isAwaited(anchor: Anchor): Boolean {
		return queuedRelativeAnchors.containsKey(anchor)
	}







	fun addToScene(trackable: Trackable, anchor: com.google.ar.core.Anchor) {
		val anchorNode = AnchorNode(sceneView.engine, anchor)
		setAnchorNode(trackable, anchorNode)
		sceneView.addChildNode(anchorNode)
	}

	fun attachModel(anchorNode: AnchorNode, model: Model, show: Boolean = true): ModelNode? {
		val modelInstance = runBlocking {
			//TODO: Confirm that this indeed fetches remote models
			val modelInstance = sceneView.modelLoader.loadModelInstance(model.href)
			return@runBlocking modelInstance
		}

		if (modelInstance == null)
			return null

		modelInstance.apply {
			//setPriority(7)
			setBlendOrder(model.zOrder ?: 0)
			setGlobalBlendOrderEnabled(true)
		}

		val modelNode = ModelNode(
			modelInstance = modelInstance
		).apply {
			isEditable = true

			//FIXME: Axis are not working
			transform(
				rotation = Rotation(model.rotationVector + Rotation(180f, 180f, 180f)),
				scale = model.scaleVector
			)

			//setPriority(7)
			setBlendOrder(model.zOrder ?: 0)
			setGlobalBlendOrderEnabled(true)
		}

		addVisualAssetToScene(
			anchorNode = anchorNode,
			visualAssetNode = modelNode,
			visualAsset = model,
			show = show
		)

		return modelNode
	}

	fun attachImage(anchorNode: AnchorNode, image: Image, show: Boolean = true): ImageNode? {
		val bitmap = runBlocking {
			//TODO: Fetch remote image
			val bitmap = BitmapFactory.decodeStream(projectAssets.open(image.href))
			return@runBlocking bitmap
		}

		if (bitmap == null)
			return null

		val imageNode = ImageNode(
			materialLoader = sceneView.materialLoader,
			bitmap = bitmap
			//normal = Direction(0f)  //TODO: Consider OrientationMode
		).apply {
			//FIXME: Once again, axis are not working
			transform(
				rotation = Rotation(image.rotationVector) + Rotation(180f, 180f, 180f)
			)
		}

		addVisualAssetToScene(
			anchorNode = anchorNode,
			visualAssetNode = imageNode,
			visualAsset = image,
			show = show
		)

		return imageNode
	}

	private fun addVisualAssetToScene(anchorNode: AnchorNode, visualAssetNode: Node, visualAsset: VisualAsset, show: Boolean = true) {
		setAnchorNode(visualAsset, anchorNode)
		setVisualAssetNode(visualAsset, visualAssetNode)

		if (visualAsset.conditions.isNotEmpty()) {
			conditionalVisualAssets.add(visualAsset)
			Log.d(TAG, "Added $visualAsset to conditionalVisualAssets")
		}

		if (show) {
			anchorNode.addChildNode(visualAssetNode)  // equivalent to show()
			Log.d(TAG, "Placed $visualAsset")
		}
	}





	fun addRelativeAnchorNode(relativeTo: RelativeTo, other: AnchorNode): AnchorNode {
		val newPos = relativeTo.geometry.let {
			when(it) {
				is Point -> it.relativeTo(other)
				else -> Position(0f,0f,0f)
			}
		}

		val newAnchorNode = AnchorNode(sceneView.engine, other.anchor).apply {
			isEditable = true
			other.addChildNode(this)

			//FIXME: Axis still don't work
			transform(
				position = newPos
			)
		}
		setAnchorNode(relativeTo, newAnchorNode)

		return newAnchorNode
	}
}
