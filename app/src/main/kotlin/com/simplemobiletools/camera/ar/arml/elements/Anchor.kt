package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element

abstract class Anchor : ARElement {
	var enabled: Boolean = true

	constructor() : super()

	constructor(other: Anchor) : super(other) {
		this.enabled = other.enabled
	}


	internal constructor(base: LowLevelAnchor) : super(base) {
		this.enabled = base.enabled ?: this.enabled
	}
}


internal abstract class LowLevelAnchor : LowLevelARElement() {

	@field:Element(name = "enabled", required = false)
	var enabled: Boolean? = null
}
