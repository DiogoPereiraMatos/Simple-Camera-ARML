package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root


@Root(name = "Orientation", strict = true)
class Orientation {

	@field:Element(name = "roll", required = false)
	var roll: Double? = null

	@field:Element(name = "tilt", required = false)
	var tilt: Double? = null

	@field:Element(name = "heading", required = false)
	var heading: Double? = null

	fun validate(): Pair<Boolean, String> {
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(roll=$roll,tilt=$tilt,heading=$heading)"
	}
}
