package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root


@Root(name = "Model", strict = true)
class Model : VisualAsset() {

	@field:Element(name = "href", required = true)
	lateinit var href: AssetRef

	@Root(name = "assetRef", strict = true)
	class AssetRef {
		@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
		@field:Attribute(name = "href", required = true)
		lateinit var href: String

		override fun toString(): String {
			return href
		}

		fun validate(): Pair<Boolean, String> {
			return Pair(true, "Success")
		}
	}

	@field:Element(name = "type", required = false)
	var type: String? = null

	@field:Element(name = "Scale", required = false)
	var scale: Scale? = null

	override fun validate(): Pair<Boolean, String> {
		val result = super.validate(); if (!result.first) return result
		if (type != null) {
			if (type!!.lowercase() !in arrayOf("normal", "infrastructure")) return Pair(
				false,
				"Expected \"normal\" or \"infrastructure\" for \"type\" element in ${this::class.simpleName}, got \"$type\""
			)
		}
		if (scale != null) {
			val result1 = scale!!.validate()
			if (!result1.first) return result1
		}
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,zOrder=$zOrder,orientation=$orientation,scalingMode=$scalingMode,conditions=$conditions,href=\"$href\",type=\"$type\",scale=$scale)"
	}
}
