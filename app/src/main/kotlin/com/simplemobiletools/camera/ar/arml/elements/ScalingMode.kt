package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

class ScalingMode internal constructor(
	private val root: ARML,
	private val base: LowLevelScalingMode
) {

	internal constructor(root: ARML, other: ScalingMode) : this(root, other.base)

	val type: String = base.type
	val minScalingDistance: Double? = base.minScalingDistance
	val maxScalingDistance: Double? = base.maxScalingDistance
	val scalingFactor: Double? = base.scalingFactor

	fun validate(): Pair<Boolean, String> {
		if (type.lowercase() !in arrayOf("natural", "custom")) return Pair(
			false,
			"Expected \"natural\" or \"custom\" for \"type\" attribute in ${this::class.simpleName}, got \"$type\""
		)
		if (minScalingDistance != null) if (minScalingDistance < 0) Pair(
			false,
			"\"minScalingDistance\" element in ${this::class.simpleName} must be >= 0, got $minScalingDistance"
		)
		if (maxScalingDistance != null) if (maxScalingDistance < 0) Pair(
			false,
			"\"maxScalingDistance\" element in ${this::class.simpleName} must be >= 0, got $maxScalingDistance"
		)
		if (scalingFactor != null) if (scalingFactor <= 0) Pair(
			false,
			"\"scalingFactor\" element in ${this::class.simpleName} must be > 0, got $scalingFactor"
		)
		if (minScalingDistance != null && maxScalingDistance != null) if (maxScalingDistance < minScalingDistance) return Pair(
			false,
			"\"maxScalingDistance\" element must greater than \"minScalingDistance\" element in ${this::class.simpleName}, got max=$maxScalingDistance and min=$minScalingDistance"
		)
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(type=\"$type\",minScalingDistance=$minScalingDistance,maxScalingDistance=$maxScalingDistance,scalingFactor=$scalingFactor)"
	}
}




@Root(name = "ScalingMode", strict = true)
internal class LowLevelScalingMode {

	@field:Attribute(name = "type", required = true)
	var type: String = "natural"

	@field:Element(name = "minScalingDistance", required = false)
	var minScalingDistance: Double? = null

	@field:Element(name = "maxScalingDistance", required = false)
	var maxScalingDistance: Double? = null

	@field:Element(name = "scalingFactor", required = false)
	var scalingFactor: Double? = null
}
