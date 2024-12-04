package com.simplemobiletools.camera.ar.arml.elements

abstract class Condition : ARElement {
	constructor() : super()
	constructor(other: Condition) : super(other)
	internal constructor(base: LowLevelCondition) : super(base)
}


internal abstract class LowLevelCondition : LowLevelARElement()
