package com.simplemobiletools.camera.ar

import android.util.Log
import com.google.ar.core.Plane
import com.simplemobiletools.camera.ar.arml.elements.*
import com.simplemobiletools.camera.ar.arml.elements.gml.LineString
import io.github.sceneview.node.Node
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
			Pair(ARElementType.FEATURE)      { it as Feature; if (it.enabled) it.handle() },

			// Anchors:
			// Just calls the relevant handler from anchorHandlers
			Pair(ARElementType.TRACKABLE)    { it as Anchor;  if (it.enabled) it.handle() },
			Pair(ARElementType.RELATIVETO)   { it as Anchor;  if (it.enabled) it.handle() },
			Pair(ARElementType.SCREENANCHOR) { it as Anchor;  if (it.enabled) it.handle() },
			Pair(ARElementType.GEOMETRY)     { it as Anchor;  if (it.enabled) it.handle() },
		)
	)

	val anchorHandlers : EnumMap<ARElementType, (Anchor) -> Unit> = EnumMap(
		mapOf<ARElementType, (Anchor) -> Unit>(
			Pair(ARElementType.TRACKABLE)    { it as Trackable;    if (it.enabled) it.handle() },
			Pair(ARElementType.RELATIVETO)   { it as RelativeTo;   if (it.enabled) it.handle() },
			Pair(ARElementType.SCREENANCHOR) { it as ScreenAnchor; if (it.enabled) Log.w(
				TAG, "Got ScreenAnchor. Ignoring...") },
			Pair(ARElementType.GEOMETRY)     { it as Geometry;     if (it.enabled) Log.w(
				TAG, "Got Geometry. Ignoring...") },
		)
	)

	val relativeToRefHandler : EnumMap<ARElementType, (RelativeToAble, RelativeTo) -> Unit> = EnumMap(
		mapOf<ARElementType, (RelativeToAble, RelativeTo) -> Unit>(
			Pair(ARElementType.TRACKABLE)  { ref, self -> ref as Trackable;  if (self.enabled) ref.handleRelativeTo(self) },
			Pair(ARElementType.RELATIVETO) { ref, self -> ref as RelativeTo; if (self.enabled) ref.handleRelativeTo(self) },
			Pair(ARElementType.GEOMETRY)   { ref, self -> ref as Geometry;   if (self.enabled) ref.handleRelativeTo(self) },
			Pair(ARElementType.MODEL)      { ref, self -> ref as Model;      if (self.enabled) Log.w(
				TAG, "Got a RelativeTo referencing a Model. That type hasn't been implemented. Ignoring...") }
		)
	)

	val trackerHandler : HashMap<String, (Trackable, TrackableConfig) -> Unit> = HashMap(
		mapOf(
			Pair("#genericPlaneTracker") { trackable, _ -> sceneController.handlePlaneTracker(trackable, Plane.Type.HORIZONTAL_UPWARD_FACING) },
			Pair("#genericHUPlaneTracker") { trackable, _ -> sceneController.handlePlaneTracker(trackable, Plane.Type.HORIZONTAL_UPWARD_FACING) },
			Pair("#genericHDPlaneTracker") { trackable, _ -> sceneController.handlePlaneTracker(trackable, Plane.Type.HORIZONTAL_DOWNWARD_FACING) },
			Pair("#genericVPlaneTracker") { trackable, _ -> sceneController.handlePlaneTracker(trackable, Plane.Type.VERTICAL) },
			Pair("#genericImageTracker") { trackable, config -> sceneController.handleImageTracker(trackable, config) },
			Pair("http://www.opengis.net/arml/tracker/genericImageTracker") { trackable, config -> sceneController.handleImageTracker(trackable, config) },
		)
	)

	val assetHandlers : EnumMap<ARElementType, (Node, VisualAsset) -> Unit> = EnumMap(
		mapOf<ARElementType, (Node, VisualAsset) -> Unit>(
			Pair(ARElementType.MODEL) { node, asset -> asset as Model; if (asset.enabled) sceneController.attachModel(node, asset) },
			Pair(ARElementType.IMAGE) { node, asset -> asset as Image; if (asset.enabled) sceneController.attachImage(node, asset) }
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
		Log.d(TAG, "Loading ARML to Scene...")
		this.arml = arml
		arml.handle()
		Log.d(TAG, "Loading ARML to Scene... DONE")
	}



	private fun ARML.handle() {
		//Log.d(TAG, this.toString())

		// Process ARML
		this.elements.forEach { it.handle() }
	}

	private fun ARElement.handle() {
		arElementHandlers.getOrElse(this.arElementType) {
			Log.w(TAG, "Got top level ${this.arElementType}. That type is not supported yet. Ignoring...")
			null
		}?.invoke(this)
	}

	private fun Feature.handle() {
		//Log.d(TAG, "Got Feature $this")

		this.anchors.forEach { anchor ->
			when (anchor) {
				is ARAnchor -> {
					anchor.assets.forEach { asset ->
						sceneState.anchorMap[asset] = anchor
						sceneState.featureMap[asset] = this
					}
				}
				is ScreenAnchor -> {
					anchor.assets.forEach { asset ->
						sceneState.anchorMap[asset] = anchor
						sceneState.featureMap[asset] = this
					}
				}
			}
		}

		this.anchors.forEach { it.handle() }
	}

	private fun Anchor.handle() {
		anchorHandlers.getOrElse(this.arElementType) {
			Log.w(TAG, "Got a ${this.arElementType} anchor. That type is not supported yet. Ignoring...")
			null
		}?.invoke(this)
	}

	private fun Trackable.handle() {
		//Log.d(TAG, "Got Trackable $this")

		this.sortedConfig.iterator().let { iterator ->
			while (iterator.hasNext()) {
				val config = iterator.next()

				// Exhausted list of configs with no match found
				if (!iterator.hasNext() && !trackerHandler.containsKey(config.tracker)) {
					Log.w(TAG, "${toShortString()} has no supported config. Ignoring...")
					return
				}

				// Not supported. Ignore.
				if (!trackerHandler.containsKey(config.tracker))
					continue

				// Supported. Handle.
				return trackerHandler[config.tracker]!!.invoke(this, config)
			}
		}
	}

	private fun RelativeTo.handle() {
		//Log.d(TAG, "Got RelativeTo $this")

		if (this.ref == "#user") {
			sceneController.addRelativeNodeToUser(this)
			return
		}

		val other = arml.elementsById[this.ref]
		if (other == null) {
			Log.w(TAG, "${toShortString()} trying to reference an element that does not exist (yet?). Ignoring...")
			return
		}
		if (other !is RelativeToAble) {
			Log.w(TAG, "${toShortString()} trying to reference an element that is not supported (${other.arElementType}). Ignoring...")
			return
		}
		relativeToRefHandler.getOrElse(other.arElementType) {
			Log.w(TAG, "Got a RelativeTo referencing a ${other.arElementType}. That type has not been implemented yet. Ignoring...")
			null
		}?.invoke(other, this)
	}

	private fun Trackable.handleRelativeTo(relativeTo: RelativeTo) {
		if (!this.enabled) {
			Log.w(TAG, "${relativeTo.toShortString()} trying to reference an element that is disabled (${toShortString()}). Ignoring...")
			return
		}

		if (sceneState.hasParentNode(this)) {
			sceneController.addRelativeNode(relativeTo, this)
			Log.d(TAG, "Created ${relativeTo.toShortString()}")
		} else {
			sceneState.addToRelativeQueue(this, relativeTo)
		}
	}

	private fun RelativeTo.handleRelativeTo(relativeTo: RelativeTo) {
		if (sceneState.hasParentNode(this)) {
			sceneController.addRelativeNode(relativeTo, this)
			Log.d(TAG, "Created ${relativeTo.toShortString()}")
		} else {
			sceneState.addToRelativeQueue(this, relativeTo)
		}
	}

	private fun Geometry.handleRelativeTo(relativeTo: RelativeTo) {
		if (relativeTo.geometry is LineString) {
			Log.w(TAG, "Got a RelativeTo referencing ${this.id} of type LineString. LineStrings are explicitly not supported. Ignoring...")
			return
		}
		Log.w(TAG, "Got a RelativeTo referencing ${this.id} of type Geometry. That type is not supported yet. Ignoring...") //TODO
	}
}
