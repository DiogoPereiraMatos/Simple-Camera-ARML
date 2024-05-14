package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

class Orientation internal constructor(
	private val root: ARML,
	private val base: LowLevelOrientation
) {

	internal constructor(root: ARML, other: Orientation) : this(root, other.base)

	val roll: Double? = base.roll
	val tilt: Double? = base.tilt
	val heading: Double? = base.heading

	fun validate(): Pair<Boolean, String> {
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(roll=$roll,tilt=$tilt,heading=$heading)"
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
