package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

class Orientation {
	var tilt: Double? = null
	var roll: Double? = null
	var heading: Double? = null

	constructor() : super()

	constructor(other: Orientation) : this() {
		this.tilt = other.tilt
		this.roll = other.roll
		this.heading = other.heading
	}

	fun validate(): Pair<Boolean, String> {
		tilt?.let { if (it < -180f || it > 180f) return Pair(false, "Tilt must be between -180 and 180. Got: $tilt") }
		roll?.let { if (it < -180f || it > 180f) return Pair(false, "Roll must be between -180 and 180. Got: $roll") }
		heading?.let { if (it < -180f || it > 180f) return Pair(false, "Heading must be between -180 and 180. Got: $heading") }
		return SUCCESS
	}

	override fun toString(): String {
		return "${this::class.simpleName}(tilt=$tilt,roll=$roll,heading=$heading)"
	}


	internal constructor(root: ARML, base: LowLevelOrientation) : this() {
		this.tilt = base.tilt
		this.roll = base.roll
		this.heading = base.heading
	}
}


@Root(name = "Orientation", strict = true)
internal class LowLevelOrientation {

	@field:Element(name = "tilt", required = false)
	var tilt: Double? = null

	@field:Element(name = "roll", required = false)
	var roll: Double? = null

	@field:Element(name = "heading", required = false)
	var heading: Double? = null
}
