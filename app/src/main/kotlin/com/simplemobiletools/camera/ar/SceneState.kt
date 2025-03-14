package com.simplemobiletools.camera.ar

import android.util.Log
import com.simplemobiletools.camera.ar.arml.elements.*
import com.simplemobiletools.camera.ar.arml.elements.gml.Point
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Size
import io.github.sceneview.node.ImageNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node

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
		+-------------+   |   +---------------+     +-------------+          +----------------------+
		|   Anchor    |   |   | (Anchor)Node  | <-- |    Anchor   | <------- | Trackable (eg Plane) |
		+-------------+   |   +---------------+     +-------------+          +----------------------+
		       |          |          |
		       |          |          |
		       v          |          v
		+-------------+   |   +-----------------------+
		|   V.Asset   |   |   |  ModelNode/ImageNode  | (VisualAssetNode)
		+-------------+   |   +-----------------------+
	 */

	// Save parent Nodes of assigned Trackables and RelativeTo
	private val assignedAnchors : HashMap<Anchor, Node> = HashMap()

	// Save parent Nodes of VisualAssets
	private val parentNodes : HashMap<VisualAsset, Node> = HashMap()

	// Save ModelNodes and ImageNodes of VisualAssets
	private val visualAssetNodes : HashMap<VisualAsset, Node> = HashMap()

	// RelativeTo waiting for Anchors go in here vvv
	private val queuedRelativeAnchors : HashMap<Anchor, ArrayList<RelativeTo>> = HashMap()

	// Save VisualAssets that have conditions
	val conditionalVisualAssets : ArrayList<VisualAsset> = ArrayList()



	companion object {
		private const val TAG = "SCENE_STATE"
	}


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
		parentNodes.clear()

		Log.d(TAG, "Reset Scene State.")
	}



	fun hasParentNode(anchor: Anchor): Boolean {
		return assignedAnchors.containsKey(anchor)
	}

	fun getParentNode(anchor: Anchor): Node? {
		return assignedAnchors[anchor]
	}

	fun setParentNode(anchor: Anchor, node: Node) {
		assignedAnchors[anchor] = node
	}

	fun hasParentNode(visualAsset: VisualAsset): Boolean {
		return parentNodes.containsKey(visualAsset)
	}

	fun getParentNode(visualAsset: VisualAsset): Node? {
		return parentNodes[visualAsset]
	}

	fun setParentNode(visualAsset: VisualAsset, node: Node) {
		parentNodes[visualAsset] = node
	}





	fun getVisualAssetNode(visualAsset: VisualAsset): Node? {
		return visualAssetNodes[visualAsset]
	}

	fun setVisualAssetNode(visualAsset: VisualAsset, visualAssetNode: Node) {
		visualAssetNodes[visualAsset] = visualAssetNode
	}

	fun getVisibility(visualAsset: VisualAsset): Boolean {
		val visualAssetNode = getVisualAssetNode(visualAsset)

		if (visualAssetNode == null) {
			Log.e(TAG, "Error getting visibility of ${visualAsset.toShortString()}. Node not found.")
			return false
		}

		return when (visualAssetNode) {
			is ModelNode -> visualAssetNode.isVisible
			is ImageNode -> visualAssetNode.isVisible
			else -> {
				Log.e(TAG, "Error getting visibility of ${visualAsset.toShortString()}. Node type not supported.")
				false
			}
		}
	}

	fun setVisibility(visualAsset: VisualAsset, visible: Boolean) {
		val visualAssetNode = getVisualAssetNode(visualAsset)

		if (visualAssetNode == null) {
			Log.e(TAG, "Error setting visibility of ${visualAsset.toShortString()}. Node not found.")
			return
		}

		when (visualAssetNode) {
			is ModelNode -> visualAssetNode.setLayerVisible(visible)
			is ImageNode -> visualAssetNode.setLayerVisible(visible)
		}
	}

	fun show(visualAsset: VisualAsset) {
		if (getVisibility(visualAsset)) {
			return
		}
		Log.d(TAG, "Showing ${visualAsset.toShortString()}")
		setVisibility(visualAsset, true)
	}

	fun hide(visualAsset: VisualAsset) {
		if (!getVisibility(visualAsset)) {
			return
		}
		Log.d(TAG, "Hiding ${visualAsset.toShortString()}")
		setVisibility(visualAsset, false)
	}







	fun addToRelativeQueue(original: Trackable, new: RelativeTo) {
		queuedRelativeAnchors.putIfAbsent(original, ArrayList())
		queuedRelativeAnchors[original]!!.add(new)
		Log.d(TAG, "Waiting for anchor for ${new.toShortString()}, aka ${original.toShortString()}")
	}

	fun addToRelativeQueue(original: RelativeTo, new: RelativeTo) {
		queuedRelativeAnchors.putIfAbsent(original, ArrayList())
		queuedRelativeAnchors[original]!!.add(new)
		Log.d(TAG, "Waiting for anchor for ${new.toShortString()}, aka ${original.toShortString()}")
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








	fun addVisualAssetToScene(node: Node, visualAssetNode: Node, visualAsset: VisualAsset, show: Boolean = true) {
		setParentNode(visualAsset, node)
		setVisualAssetNode(visualAsset, visualAssetNode)

		setVisibility(visualAsset, show)

		node.addChildNode(visualAssetNode)
		Log.d(TAG, "Placed ${visualAsset.toShortString()}")

		if (visualAsset.conditions.isNotEmpty()) {
			conditionalVisualAssets.add(visualAsset)
			Log.d(TAG, "Added ${visualAsset.toShortString()} to conditionalVisualAssets")
		}
	}





	fun addRelativeAnchorNode(relativeTo: RelativeTo, other: AnchorNode): AnchorNode {
		val newPos = relativeTo.geometry.let {
			when(it) {
				is Point -> it.asVec3
				else -> Position(0f,0f,0f)
			}
		}

		val newAnchorNode = AnchorNode(sceneView.engine, other.anchor).apply {
			isEditable = true
			other.addChildNode(this)

			transform(
				position = newPos,
				rotation = Rotation(0f, 0f, 0f),
				scale = Size(1f, 1f, 1f)
			)
		}
		setParentNode(relativeTo, newAnchorNode)

		return newAnchorNode
	}

	fun addRelativeNode(relativeTo: RelativeTo, other: Node): Node {
		val newPos = relativeTo.geometry.let {
			when(it) {
				is Point -> it.asVec3
				else -> Position(0f,0f,0f)
			}
		}

		val newNode = Node(sceneView.engine).apply {
			isEditable = true
			other.addChildNode(this)

			transform(
				position = newPos,
				rotation = Rotation(0f, 0f, 0f),
				scale = Size(1f, 1f, 1f)
			)
		}
		setParentNode(relativeTo, newNode)

		return newNode
	}

	fun addRelativeNodeToUser(relativeTo: RelativeTo): Node {
		val newPos = relativeTo.geometry.let {
			when(it) {
				is Point -> it.asVec3
				else -> Position(0f,0f,0f)
			}
		}

		val newNode = Node(sceneView.engine).apply {
			isEditable = true

			//FIXME: Don't know how multiple parents work... but ok
			sceneView.addChildNode(this) // Camera node is not part of scene so ya have to do this. TODO: Add cameraNode to scene at start?
			sceneView.cameraNode.addChildNode(this) // This makes it so Sceneview keeps track of the camera orientation, otherwise it leaves the assets where it created them.

			transform(
				position = newPos,
				rotation = Rotation(0f, 0f, 0f),
				scale = Size(1f, 1f, 1f)
			)
		}
		setParentNode(relativeTo, newNode)

		return newNode
	}




	val featureMap : HashMap<VisualAsset, Feature> = HashMap()
	fun getFeature(visualAsset: VisualAsset): Feature {
		return featureMap[visualAsset]!!
	}

	val anchorMap : HashMap<VisualAsset, Anchor> = HashMap()
	fun getAnchor(visualAsset: VisualAsset): Anchor {
		return anchorMap[visualAsset]!!
	}
}
