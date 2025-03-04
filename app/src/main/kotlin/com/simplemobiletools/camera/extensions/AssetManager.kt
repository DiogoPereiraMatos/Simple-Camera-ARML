package com.simplemobiletools.camera.extensions

import android.content.res.AssetManager

fun AssetManager.listFilesInDirectory(path: String): ArrayList<String>? {
	val fileOrFolder = this.list(path) ?: return null
	if (fileOrFolder.isEmpty())
		return null

	val allAssets = ArrayList<String>()
	for (f in fileOrFolder) {
		val recursive = this.listFilesInDirectory("$path/$f")
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
