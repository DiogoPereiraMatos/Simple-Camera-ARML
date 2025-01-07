package com.simplemobiletools.camera.ar.modules

interface ARModule {
	fun enable()
	fun disable()
	fun reset()
	fun isEnabled() : Boolean
}
