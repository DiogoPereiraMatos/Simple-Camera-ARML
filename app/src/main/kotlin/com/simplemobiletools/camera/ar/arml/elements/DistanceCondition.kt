package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

class DistanceCondition : Condition {
	override val arElementType = ARElementType.DISTANCECONDITION

	var min: Double? = null
	var max: Double? = null

	constructor() : super()

	constructor(other: DistanceCondition) : super(other) {
		this.min = other.min
		this.max = other.max
	}

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun validate(): Pair<Boolean, String> {
		if (min != null && min!! < 0)
			return Pair(
				false,
				"\"min\" element in ${this::class.simpleName} must be >= 0, got $min"
			)
		if (max != null && max!! < 0)
			return Pair(
				false,
				"\"max\" element in ${this::class.simpleName} must be >= 0, got $max"
			)
		if (min != null && max != null && max!! < min!!)
			return Pair(
				false,
				"\"max\" element must greater than \"min\" element in ${this::class.simpleName}, got max=$max and min=$min"
			)

		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelDistanceCondition) : super(base) {
		this.min = base.min
		this.max = base.max
	}
}


@Root(name = "DistanceCondition", strict = true)
internal class LowLevelDistanceCondition : LowLevelCondition() {

	@field:Element(name = "min", required = false)
	var min: Double? = null

	@field:Element(name = "max", required = false)
	var max: Double? = null
}
