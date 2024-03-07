package com.simplemobiletools.camera.ar.armlparser.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root


@Root(name = "DistanceCondition", strict = true)
class DistanceCondition : Condition() {

	@field:Element(name = "min", required = false)
	var min: Double? = null

	@field:Element(name = "max", required = false)
	var max: Double? = null

	override fun validate(): Pair<Boolean, String> {
		if (min != null) if (min!! < 0) return Pair(
			false,
			"\"min\" element in ${this::class.simpleName} must be >= 0, got $min"
		)
		if (max != null) if (max!! < 0) return Pair(
			false,
			"\"max\" element in ${this::class.simpleName} must be >= 0, got $max"
		)
		if (min != null && max != null) if (max!! < min!!) return Pair(
			false,
			"\"max\" element must greater than \"min\" element in ${this::class.simpleName}, got max=$max and min=$min"
		)
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",min=$min,max=$max)"
	}
}
