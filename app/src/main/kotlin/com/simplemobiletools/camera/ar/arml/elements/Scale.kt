package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

class Scale internal constructor(
	private val root: ARML,
	private val base: LowLevelScale
) {

	internal constructor(root: ARML, other: Scale) : this(root, other.base)

	val x: Double? = base.x
	val y: Double? = base.y
	val z: Double? = base.z

	fun validate(): Pair<Boolean, String> {
		if (x != null) if (x < 0) return Pair(
			false,
			"\"x\" element in ${this::class.simpleName} must be >= 0, got $x"
		)
		if (y != null) if (y < 0) return Pair(
			false,
			"\"y\" element in ${this::class.simpleName} must be >= 0, got $y"
		)
		if (z != null) if (z < 0) return Pair(
			false,
			"\"z\" element in ${this::class.simpleName} must be >= 0, got $z"
		)
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(x=$x,y=$y,z=$z)"
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
