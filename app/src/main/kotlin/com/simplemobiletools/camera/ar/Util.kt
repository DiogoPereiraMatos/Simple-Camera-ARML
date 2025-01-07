package com.simplemobiletools.camera.ar

import android.content.res.AssetManager
import com.simplemobiletools.camera.ar.arml.elements.Trackable
import com.simplemobiletools.camera.ar.arml.elements.gml.Point
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position


fun AssetManager.listAssets(path: String): ArrayList<String>? {
	val fileOrFolder = this.list(path) ?: return null
	if (fileOrFolder.isEmpty())
		return null

	val allAssets = ArrayList<String>()
	for (f in fileOrFolder) {
		val recursive = this.listAssets("$path/$f")
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

fun ArrayList<Trackable>.putIfAbsent(element: Trackable) {
	if (!this.contains(element)) {
		this.add(element)
	}
}

fun Point.relativeTo(anchorNode: AnchorNode): Position {
	//Let's assume 3 dimensions
	return anchorNode.position.plus(Float3(this.pos[0], this.pos[1], this.pos[2]))
}
