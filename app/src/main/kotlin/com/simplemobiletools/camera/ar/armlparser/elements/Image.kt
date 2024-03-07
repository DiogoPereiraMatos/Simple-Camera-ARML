package com.simplemobiletools.camera.ar.armlparser.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root


@Root(name = "Image", strict = true)
class Image : VisualAsset2D() {

	@field:Element(name = "href", required = true)
	lateinit var href: HREF

	@Root(name = "href")
	class HREF {
		@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
		@field:Attribute(name = "href", required = true)
		lateinit var href: String

		override fun toString(): String {
			return href
		}
	}

	override fun validate(): Pair<Boolean, String> {
		val result = super.validate(); if (!result.first) return result
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,zOrder=$zOrder,orientation=$orientation,scalingMode=$scalingMode,conditions=$conditions,width=\"$width\",height=\"$height\",orientationMode=\"$orientationMode\",backside=\"$backside\",href=\"$href\")"
	}
}
