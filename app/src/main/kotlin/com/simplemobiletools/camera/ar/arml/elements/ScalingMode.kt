package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

enum class ScalingModeType {
	NATURAL,
	CUSTOM;

	override fun toString(): String {
		return this.name.lowercase()
	}
}

class ScalingMode {
	var type: ScalingModeType
	var minScalingDistance: Double? = null
	var maxScalingDistance: Double? = null
	var scalingFactor: Double? = null

	constructor(type: ScalingModeType) : super() {
		this.type = type
	}

	constructor(type: String) : super() {
		try {
			this.type = ScalingModeType.valueOf(type.uppercase())
		} catch (e: IllegalArgumentException) {
			val possibleValues = ScalingModeType.entries.map { it.toString() }
			throw Exception("Expected one of $possibleValues for \"type\" attribute in ${this::class.simpleName}, got \"$type\"")
		}
	}

	constructor(other: ScalingMode) : this(other.type) {
		this.minScalingDistance = other.minScalingDistance
		this.maxScalingDistance = other.maxScalingDistance
		this.scalingFactor = other.scalingFactor
	}

	fun validate(): Pair<Boolean, String> {
		if (minScalingDistance != null && minScalingDistance!! < 0)
			return Pair(
				false,
				"\"minScalingDistance\" element in ${this::class.simpleName} must be >= 0, got $minScalingDistance"
			)
		if (maxScalingDistance != null && maxScalingDistance!! < 0)
			return Pair(
				false,
				"\"maxScalingDistance\" element in ${this::class.simpleName} must be >= 0, got $maxScalingDistance"
			)
		if (scalingFactor != null && scalingFactor!! <= 0)
			return Pair(
				false,
				"\"scalingFactor\" element in ${this::class.simpleName} must be > 0, got $scalingFactor"
			)
		if (minScalingDistance != null && maxScalingDistance != null && maxScalingDistance!! < minScalingDistance!!)
			return Pair(
				false,
				"\"maxScalingDistance\" element must greater than \"minScalingDistance\" element in ${this::class.simpleName}, got max=$maxScalingDistance and min=$minScalingDistance"
			)

		return SUCCESS
	}

	override fun toString(): String {
		return "${this::class.simpleName}(type=\"$type\",minScalingDistance=$minScalingDistance,maxScalingDistance=$maxScalingDistance,scalingFactor=$scalingFactor)"
	}


	internal constructor(root: ARML, base: LowLevelScalingMode) : this(base.type) {
		this.minScalingDistance = base.minScalingDistance
		this.maxScalingDistance = base.maxScalingDistance
		this.scalingFactor = base.scalingFactor
	}
}


@Root(name = "ScalingMode", strict = true)
internal class LowLevelScalingMode {

	@field:Attribute(name = "type", required = true)
	lateinit var type: String

	@field:Element(name = "minScalingDistance", required = false)
	var minScalingDistance: Double? = null

	@field:Element(name = "maxScalingDistance", required = false)
	var maxScalingDistance: Double? = null

	@field:Element(name = "scalingFactor", required = false)
	var scalingFactor: Double? = null
}
