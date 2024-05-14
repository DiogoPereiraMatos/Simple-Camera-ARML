package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

class Text internal constructor(
	private val root: ARML,
	private val base: LowLevelText
) : VisualAsset2D(root, base) {

	internal constructor(root: ARML, other: Text) : this(root, other.base)

	val src: String = base.src
	val style: String? = base.style
	val css: String? = base.css

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun validate(): Pair<Boolean, String> {
		val result = super.validate(); if (!result.first) return result
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,zOrder=$zOrder,orientation=$orientation,scalingMode=$scalingMode,conditions=$conditions,width=\"$width\",height=\"$height\",orientationMode=\"$orientationMode\",backside=\"$backside\",src=\"$src\",style=\"$style\",class=\"$css\")"
	}
}




@Root(name = "Text", strict = true)
internal class LowLevelText : LowLevelVisualAsset2D() {

	@field:Element(name = "src", required = true)
	var src: String = ""

	@field:Element(name = "style", required = false)
	var style: String? = null

	@field:Element(name = "class", required = false)
	var css: String? = null
}
