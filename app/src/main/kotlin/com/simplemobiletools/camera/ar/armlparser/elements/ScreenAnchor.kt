package com.simplemobiletools.camera.ar.armlparser.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

@Root(name = "ScreenAnchor", strict = true)
class ScreenAnchor : Anchor() {

	@field:Element(name = "style", required = false)
	var style: String? = null

	@field:Element(name = "class", required = false)
	var css: String? = null

	@field:Element(name = "assets", required = true)
	lateinit var assets: ScreenAnchorAssets

	@Root(name = "assets", strict = true)
	class ScreenAnchorAssets {

		@field:ElementList(name = "assetRef", type = AssetRef::class, inline = true, required = false)
		var assetsRefs: List<AssetRef> = ArrayList()

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

		@field:ElementList(name = "Label", type = Label::class, inline = true, required = false)
		var labels: List<VisualAsset> = ArrayList()

		override fun toString(): String {
			val result = ArrayList<Any>()
			result.addAll(assetsRefs)
			result.addAll(labels)
			return result.toString()
		}

		fun validate(): Pair<Boolean, String> {
			assetsRefs.forEach { val result = it.validate(); if (!result.first) return result }
			labels.forEach { val result1 = it.validate(); if (!result1.first) return result1 }
			return Pair(true, "Success")
		}
	}

	override fun toString(): String {
		return "ScreenAnchor(id=\"$id\",enabled=$enabled,style=\"$style\",class=\"$css\",assets=$assets)"
	}

	override fun validate(): Pair<Boolean, String> {
		val result = super.validate(); if (!result.first) return result
		val result1 = assets.validate(); if (!result1.first) return result1
		return Pair(true, "Success")
	}
}
