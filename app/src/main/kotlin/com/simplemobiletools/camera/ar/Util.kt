package com.simplemobiletools.camera.ar

import android.content.res.AssetManager
import com.simplemobiletools.camera.ar.arml.elements.Trackable


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
