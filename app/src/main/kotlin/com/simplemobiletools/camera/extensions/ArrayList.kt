package com.simplemobiletools.camera.extensions

fun<T> ArrayList<T>.putIfAbsent(element: T) {
	if (!this.contains(element)) {
		this.add(element)
	}
}
