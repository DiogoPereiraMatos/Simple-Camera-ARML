package com.simplemobiletools.camera.ar

import android.util.Log
import com.google.ar.core.Plane
import com.simplemobiletools.camera.ar.arml.elements.*
import com.simplemobiletools.camera.ar.arml.elements.gml.LineString
import io.github.sceneview.ar.node.AnchorNode
import java.util.EnumMap

class SceneLoader(
	private val sceneController: SceneController,
	private val sceneState: SceneState,
) {
	/*
                                                            +----------+
                                                            |   ARML   |
                                                            +----------+
                                                                  1
                                                                  |
                                                                0..*
                                        -------------------------------------------------------------   ...
                                        |                                 |
                                +---------------+                 +---------------+
                                | Trackable     |                 | Trackable     |
                                | (Anchor Node) |                 | (Anchor Node) |
                                +---------------+                 +---------------+
                                      0..1
                                        |
                                      0..*
               --------------------------------------------------------------   ...
               |                    |                        |
        +-------------+      +-------------+          +--------------+
        | Model       |      | Image       |          | RelativeTo   |
        | (ModelNode) |      | (ImageNode) |          | (AnchorNode) |
        +-------------+      +-------------+          +--------------+
                                                             |
                                                            ...
	 */

	val arElementHandlers : EnumMap<ARElementType, (ARElement) -> Unit> = EnumMap(
		mapOf<ARElementType, (ARElement) -> Unit>(
			Pair(ARElementType.FEATURE)      { it as Feature; it.handle() },
			Pair(ARElementType.TRACKABLE)    { it as Anchor;  it.handle() },
			Pair(ARElementType.RELATIVETO)   { it as Anchor;  it.handle() },
			Pair(ARElementType.SCREENANCHOR) { it as Anchor;  it.handle() },
			Pair(ARElementType.GEOMETRY)     { it as Anchor;  it.handle() },
		)
	)

	val anchorHandlers : EnumMap<ARElementType, (Anchor) -> Unit> = EnumMap(
		mapOf<ARElementType, (Anchor) -> Unit>(
			Pair(ARElementType.TRACKABLE)    { it as Trackable;    it.handle() },
			Pair(ARElementType.RELATIVETO)   { it as RelativeTo;   it.handle() },
			Pair(ARElementType.SCREENANCHOR) { it as ScreenAnchor; if (it.enabled) Log.w(
				TAG, "Got ScreenAnchor. Ignoring...") },
			Pair(ARElementType.GEOMETRY)     { it as Geometry;     if (it.enabled) Log.w(
				TAG, "Got Geometry. Ignoring...") },
		)
	)

	val relativeToRefHandler : EnumMap<ARElementType, (RelativeToAble, RelativeTo) -> Unit> = EnumMap(
		mapOf<ARElementType, (RelativeToAble, RelativeTo) -> Unit>(
			Pair(ARElementType.TRACKABLE)  { ref, self -> ref as Trackable;  ref.handleRelativeTo(self) },
			Pair(ARElementType.RELATIVETO) { ref, self -> ref as RelativeTo; ref.handleRelativeTo(self) },
			Pair(ARElementType.GEOMETRY)   { ref, self -> ref as Geometry;   ref.handleRelativeTo(self) },
			Pair(ARElementType.MODEL)      { ref, self -> ref as Model;      if (self.enabled) Log.w(
				TAG, "Got a RelativeTo referencing a Model. That type hasn't been implemented.") }
		)
	)

	val trackerHandler : HashMap<String, (Trackable, TrackableConfig) -> Unit> = HashMap(
		mapOf(
			Pair("#genericPlaneTracker") { trackable, _ -> sceneController.handlePlaneTracker(trackable, Plane.Type.HORIZONTAL_UPWARD_FACING) },
			Pair("#genericHUPlaneTracker") { trackable, _ -> sceneController.handlePlaneTracker(trackable, Plane.Type.HORIZONTAL_UPWARD_FACING) },
			Pair("#genericHDPlaneTracker") { trackable, _ -> sceneController.handlePlaneTracker(trackable, Plane.Type.HORIZONTAL_DOWNWARD_FACING) },
			Pair("#genericVPlaneTracker") { trackable, _ -> sceneController.handlePlaneTracker(trackable, Plane.Type.VERTICAL) },
			Pair("#genericImageTracker") { trackable, config -> sceneController.handleImageTracker(trackable, config) },
		)
	)

	val assetHandlers : EnumMap<ARElementType, (AnchorNode, VisualAsset) -> Unit> = EnumMap(
		mapOf<ARElementType, (AnchorNode, VisualAsset) -> Unit>(
			Pair(ARElementType.MODEL) { anchor, asset -> asset as Model; sceneController.attachModel(anchor, asset) },
			Pair(ARElementType.IMAGE) { anchor, asset -> asset as Image; sceneController.attachImage(anchor, asset) }
		)
	)

	val conditionHandlers : EnumMap<ARElementType, (VisualAsset, Condition) -> Boolean> = EnumMap(
		mapOf<ARElementType, (VisualAsset, Condition) -> Boolean>(
			Pair(ARElementType.SELECTEDCONDITION) { asset, condition -> condition as SelectedCondition; sceneController.selectionModule.evaluateCondition(asset, condition) },
			Pair(ARElementType.DISTANCECONDITION) { asset, condition -> condition as DistanceCondition; sceneController.distanceModule.evaluateCondition(asset, condition) },
		)
	)



	companion object {
		private const val TAG = "SCENE_LOADER"
	}


	private lateinit var arml: ARML
	fun load(arml: ARML) {
		this.arml = arml
		return arml.handle()
	}



	private fun ARML.handle() {
		Log.d(TAG, this.toString())

		// Process ARML
		this.elements.forEach { it.handle() }
	}

	private fun ARElement.handle() {
		arElementHandlers.getOrElse(this.arElementType) {
			Log.w(TAG, "Got top level ${this.arElementType}. That type is not supported yet.")
			null
		}?.invoke(this)
	}

	private fun Feature.handle() {
		if (!this.enabled) return
		//Log.d(TAG, "Got Feature $this")
		this.anchors.forEach { it.handle() }
	}

	private fun Anchor.handle() {
		if (!this.enabled) return
		anchorHandlers.getOrElse(this.arElementType) {
			Log.w(TAG, "Got a ${this.arElementType} anchor. That type is not supported yet.")
			null
		}?.invoke(this)
	}

	private fun Trackable.handle() {
		if (!this.enabled) return
		//Log.d(TAG, "Got Trackable $this")

		//TODO: Is it really supposed to process every config?
		this.sortedConfig.forEach {
			trackerHandler.getOrElse(it.tracker) {
				Log.w(TAG, "Got an unknown tracker: ${it.tracker}")
				null
			}?.invoke(this, it)
		}
	}

	private fun RelativeTo.handle() {
		if (!this.enabled) return
		//Log.d(TAG, "Got RelativeTo $this")

		if (this.ref == "#user") {
			//TODO
			return
		}

		val other = arml.elementsById[this.ref]
		if (other == null) {
			Log.w(TAG, "RelativeTo $this trying to reference an element that does not exist (yet?).")
			return
		}
		if (other !is RelativeToAble) {
			Log.w(TAG, "RelativeTo $this trying to reference an element that is not supported (${other.arElementType}).")
			return
		}
		relativeToRefHandler.getOrElse(other.arElementType) {
			Log.w(TAG, "Got a RelativeTo referencing a ${other.arElementType}. That type has not been implemented yet.")
			null
		}?.invoke(other, this)
	}

	private fun Trackable.handleRelativeTo(relativeTo: RelativeTo) {
		if (!this.enabled) {
			Log.w(TAG, "RelativeTo $relativeTo trying to reference an element that is disabled ($this).")
			return
		}

		if (sceneState.hasAnchorNode(this)) {
			sceneController.addRelativeAnchorNode(relativeTo, this)
			Log.d(TAG, "Created $relativeTo")
		} else {
			sceneState.addToRelativeQueue(this, relativeTo)
		}
	}

	private fun RelativeTo.handleRelativeTo(relativeTo: RelativeTo) {
		if (sceneState.hasAnchorNode(this)) {
			sceneController.addRelativeAnchorNode(relativeTo, this)
			Log.d(TAG, "Created $relativeTo")
		} else {
			sceneState.addToRelativeQueue(this, relativeTo)
		}
	}

	private fun Geometry.handleRelativeTo(relativeTo: RelativeTo) {
		if (relativeTo.geometry is LineString) {
			Log.w(TAG, "Got a RelativeTo referencing $this of type LineString. LineStrings are explicitly not supported.")
			return
		}
		Log.w(TAG, "Got a RelativeTo referencing $this of type Geometry. That type is not supported yet.") //TODO
	}
}
