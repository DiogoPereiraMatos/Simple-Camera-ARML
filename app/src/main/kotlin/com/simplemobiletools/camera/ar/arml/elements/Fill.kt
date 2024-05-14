package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

class Fill internal constructor(
	private val root: ARML,
	private val base: LowLevelFill
) : VisualAsset2D(root, base) {

	internal constructor(root: ARML, other: Fill) : this(root, other.base)

	val style: String? = base.style
	val css: String? = base.css

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun validate(): Pair<Boolean, String> {
		val result = super.validate(); if (!result.first) return result
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,zOrder=$zOrder,orientation=$orientation,scalingMode=$scalingMode,conditions=$conditions,width=\"$width\",height=\"$height\",orientationMode=\"$orientationMode\",backside=\"$backside\",style=\"$style\",class=\"$css\")"
	}
}




@Root(name = "Fill", strict = true)
internal class LowLevelFill : LowLevelVisualAsset2D() {

	@field:Element(name = "style", required = false)
	var style: String? = null

	@field:Element(name = "class", required = false)
	var css: String? = null
}
