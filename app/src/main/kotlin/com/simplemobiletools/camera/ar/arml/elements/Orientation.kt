package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

class Orientation {
	var roll: Double? = null
	var tilt: Double? = null
	var heading: Double? = null

	constructor() : super()

	constructor(other: Orientation) : this() {
		this.roll = other.roll
	}

	fun validate(): Pair<Boolean, String> = SUCCESS

	override fun toString(): String {
		return "${this::class.simpleName}(roll=$roll,tilt=$tilt,heading=$heading)"
	}


	internal constructor(root: ARML, base: LowLevelOrientation) : this() {
		this.roll = base.roll
		this.tilt = base.tilt
		this.heading = base.heading
	}
}


@Root(name = "Orientation", strict = true)
internal class LowLevelOrientation {

	@field:Element(name = "roll", required = false)
	var roll: Double? = null

	@field:Element(name = "tilt", required = false)
	var tilt: Double? = null

	@field:Element(name = "heading", required = false)
	var heading: Double? = null
}
