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

abstract class VisualAsset2D : VisualAsset {
	var width: String? = null
	var height: String? = null
	var orientationMode: OrientationMode? = null
	var backside: String? = null

	constructor() : super()

	constructor(other: VisualAsset2D) : super(other) {
		this.width = other.width
		this.height = other.height
		this.orientationMode = other.orientationMode
		this.backside = other.backside
	}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,zOrder=$zOrder,orientation=$orientation,scalingMode=$scalingMode,conditions=$conditions,width=\"$width\",height=\"$height\",orientationMode=\"$orientationMode\",backside=\"$backside\")"
	}


	internal constructor(root: ARML, base: LowLevelVisualAsset2D) : super(root, base) {
		this.width = base.width
		this.height = base.height
		this.backside = base.backside

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
