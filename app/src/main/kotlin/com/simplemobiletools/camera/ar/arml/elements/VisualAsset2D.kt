package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element

enum class OrientationMode {
	USER,
	ABSOLUTE,
	AUTO;

	override fun toString(): String {
		return this.name.lowercase()
	}
}

abstract class Size
class SizeAbsolute(val m: Float) : Size()
class SizePercentage(val p: Float) : Size()

abstract class VisualAsset2D : VisualAsset {
	var width: Size? = null
	var height: Size? = null
	var orientationMode: OrientationMode? = null
	var backside: String? = null

	constructor() : super()

	constructor(other: VisualAsset2D) : super(other) {
		this.width = other.width
		this.height = other.height
		this.orientationMode = other.orientationMode
		this.backside = other.backside
	}

	override fun validate(): Pair<Boolean, String> {
		super.validate().let { if (!it.first) return it }
		this.width?.let { when (it) {
			is SizePercentage -> if (it.p <= 0) return Pair(false, "Invalid width percentage: ${it.p}")
			is SizeAbsolute -> if (it.m <= 0) return Pair(false, "Invalid width absolute: ${it.m}")
		} }
		this.height?.let { when (it) {
			is SizePercentage -> if (it.p <= 0) return Pair(false, "Invalid height percentage: ${it.p}")
			is SizeAbsolute -> if (it.m <= 0) return Pair(false, "Invalid height absolute: ${it.m}")
		} }
		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelVisualAsset2D) : super(root, base) {
		this.backside = base.backside

		this.width = base.width?.let {
			if (it.matches(Regex(".+%")))
				SizePercentage(it.removeSuffix("%").toFloat())
			else
				SizeAbsolute(it.toFloat())
		}

		this.height = base.height?.let {
			if (it.matches(Regex(".+%"))) {
				SizePercentage(it.removeSuffix("%").toFloat())
			} else
				SizeAbsolute(it.toFloat())
		}


		if (base.orientationMode != null) {
			try {
				this.orientationMode = OrientationMode.valueOf(base.orientationMode!!.uppercase())
			} catch (e: IllegalArgumentException) {
				val possibleValues = OrientationMode.entries.map { it.toString() }
				throw Exception("Expected one of $possibleValues for \"orientationMode\" element in ${this::class.simpleName}, got \"${base.orientationMode}\"")
			}
		}
	}
}


internal abstract class LowLevelVisualAsset2D : LowLevelVisualAsset() {

	@field:Element(name = "width", required = false)
	var width: String? = null

	@field:Element(name = "height", required = false)
	var height: String? = null

	@field:Element(name = "orientationMode", required = false)
	var orientationMode: String? = null

	@field:Element(name = "backside", required = false)
	var backside: String? = null
}
