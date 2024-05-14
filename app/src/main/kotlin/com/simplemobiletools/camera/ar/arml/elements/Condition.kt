package com.simplemobiletools.camera.ar.arml.elements

abstract class Condition internal constructor(
	private val root: ARML,
	private val base: LowLevelCondition
) : ARElement(root, base)




//REQ: http://www.opengis.net/spec/arml/2.0/req/model/Condition/interface
internal abstract class LowLevelCondition : LowLevelARElement()
