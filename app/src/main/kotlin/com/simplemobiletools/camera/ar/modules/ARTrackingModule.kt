package com.simplemobiletools.camera.ar.modules

import com.google.ar.core.Frame
import com.simplemobiletools.camera.ar.SceneController

interface ARTrackingModule: ARModule {
	fun onFrameUpdate(context: SceneController, frame: Frame)
}
