package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

class Scale {
	var x: Double = 1.0
	var y: Double = 1.0
	var z: Double = 1.0

	constructor() : super()

	constructor(other: Scale) : this() {
		this.x = other.x
		this.y = other.y
		this.z = other.z
	}

	fun validate(): Pair<Boolean, String> {
		if (x <= 0)
			return Pair(
				false,
				"\"x\" element in ${this::class.simpleName} must be > 0, got $x"
			)
		if (y <= 0)
			return Pair(
				false,
				"\"y\" element in ${this::class.simpleName} must be > 0, got $y"
			)
		if (z <= 0)
			return Pair(
				false,
				"\"z\" element in ${this::class.simpleName} must be > 0, got $z"
			)

		return SUCCESS
	}

	override fun toString(): String {
		return "${this::class.simpleName}(x=$x,y=$y,z=$z)"
	}


	internal constructor(root: ARML, base: LowLevelScale) : this() {
		this.x = base.x ?: 1.0
		this.y = base.y ?: 1.0
		this.z = base.z ?: 1.0
	}
}


@Root(name = "Scale", strict = true)
internal class LowLevelScale {

	@field:Element(name = "x", required = false)
	var x: Double? = null

	@field:Element(name = "y", required = false)
	var y: Double? = null

	@field:Element(name = "z", required = false)
	var z: Double? = null
}
