package com.simplemobiletools.camera.ar.modules

import com.simplemobiletools.camera.ar.arml.elements.Condition
import com.simplemobiletools.camera.ar.arml.elements.VisualAsset

interface ARConditionModule: ARModule {
	fun evaluateCondition(visualAsset: VisualAsset, condition: Condition): Boolean
}
