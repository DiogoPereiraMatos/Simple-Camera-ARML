package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root


@Root(name = "Text", strict = true)
class Text : VisualAsset2D() {

	@field:Element(name = "src", required = true)
	var src: String = ""

	@field:Element(name = "style", required = false)
	var style: String? = null

	@field:Element(name = "class", required = false)
	var css: String? = null

	override fun validate(): Pair<Boolean, String> {
		val result = super.validate(); if (!result.first) return result
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,zOrder=$zOrder,orientation=$orientation,scalingMode=$scalingMode,conditions=$conditions,width=\"$width\",height=\"$height\",orientationMode=\"$orientationMode\",backside=\"$backside\",src=\"$src\",style=\"$style\",class=\"$css\")"
	}
}