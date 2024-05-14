package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class ScreenAnchor internal constructor(
	private val root: ARML,
	private val base: LowLevelScreenAnchor
) : Anchor(root, base) {

	internal constructor(root: ARML, other: ScreenAnchor) : this(root, other.base)

	val style: String? = base.style
	val css: String? = base.css

	val assets: ArrayList<Any>
		get() {
			val result = ArrayList<Any>()
			result.addAll(base.assets.assetsRefs)
			base.assets.labels.forEach {
				when(it) {
					is LowLevelLabel -> result.add(Label(root, it))
					else -> throw Exception("Unexpected ScreenAnchor Asset Type: $it")
				}
			}
			return result
		}

	override val elementsById: HashMap<String, ARElement>
		get() {
			val result: HashMap<String, ARElement> = HashMap()
			assets.forEach {
				when(it) {
					is Label -> { it.id?.let { id -> result[id] = it}; result.putAll(it.elementsById) }
					else -> throw Exception("Unexpected ScreenAnchor Asset Type: $it")
				}
			}
			return result
		}

	override fun toString(): String {
		return "ScreenAnchor(id=\"$id\",enabled=$enabled,style=\"$style\",class=\"$css\",assets=$assets)"
	}

	override fun validate(): Pair<Boolean, String> {
		val result = super.validate(); if (!result.first) return result
		assets.filterIsInstance(VisualAsset::class.java).forEach { val result1 = it.validate(); if (!result1.first) return result1 }
		return Pair(true, "Success")
	}
}




@Root(name = "ScreenAnchor", strict = true)
internal class LowLevelScreenAnchor : LowLevelAnchor() {

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
		}

		@field:ElementList(name = "Label", type = LowLevelLabel::class, inline = true, required = false)
		var labels: List<LowLevelVisualAsset> = ArrayList()
	}
}
