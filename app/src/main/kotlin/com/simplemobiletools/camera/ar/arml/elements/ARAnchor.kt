package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementListUnion
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

//REQ: http://www.opengis.net/spec/arml/2.0/req/model/ARAnchor/interface
abstract class ARAnchor : Anchor() {

	@field:Element(name = "assets", required = true)
	lateinit var assets: ARAnchorAssets

	@Root(name = "assets", strict = true)
	class ARAnchorAssets {

		//REQ: http://www.opengis.net/spec/arml/2.0/req/model/ARAnchor/relative
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

		@field:ElementListUnion(
			// VisualAssets
			ElementList(name = "Model", type = Model::class, inline = true, required = false),
			// VisualAssets2D
			ElementList(name = "Fill", type = Fill::class, inline = true, required = false),
			ElementList(name = "Image", type = Image::class, inline = true, required = false),
			ElementList(name = "Label", type = Label::class, inline = true, required = false),
			ElementList(name = "Text", type = Text::class, inline = true, required = false),
		)
		var assets: List<VisualAsset> = ArrayList()

		override fun toString(): String {
			val result = ArrayList<Any>()
			result.addAll(assetsRefs)
			result.addAll(assets)
			return result.toString()
		}

		fun validate(): Pair<Boolean, String> {
			assetsRefs.forEach { val result = it.validate(); if (!result.first) return result }
			assets.forEach { val result1 = it.validate(); if (!result1.first) return result1 }
			return Pair(true, "Success")
		}
	}


	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,assets=$assets)"
	}

	override fun validate(): Pair<Boolean, String> {
		val result = super.validate(); if (!result.first) return result
		val result1 = assets.validate(); if (!result1.first) return result1
		return Pair(true, "Success")
	}
}
