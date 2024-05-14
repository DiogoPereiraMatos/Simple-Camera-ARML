package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

class DistanceCondition internal constructor(
	private val root: ARML,
	private val base: LowLevelDistanceCondition
) : Condition(root, base) {

	val min: Double? = base.min
	val max: Double? = base.max

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun validate(): Pair<Boolean, String> {
		if (min != null) if (min < 0) return Pair(
			false,
			"\"min\" element in ${this::class.simpleName} must be >= 0, got $min"
		)
		if (max != null) if (max < 0) return Pair(
			false,
			"\"max\" element in ${this::class.simpleName} must be >= 0, got $max"
		)
		if (min != null && max != null) if (max < min) return Pair(
			false,
			"\"max\" element must greater than \"min\" element in ${this::class.simpleName}, got max=$max and min=$min"
		)
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",min=$min,max=$max)"
	}
}




@Root(name = "DistanceCondition", strict = true)
internal class LowLevelDistanceCondition : LowLevelCondition() {

	@field:Element(name = "min", required = false)
	var min: Double? = null

	@field:Element(name = "max", required = false)
	var max: Double? = null
}
