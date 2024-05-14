package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementListUnion
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

abstract class ARAnchor internal constructor(
	private val root: ARML,
	private val base: LowLevelARAnchor
) : Anchor(root, base) {

	internal constructor(root: ARML, other: ARAnchor) : this(root, other.base)

	val assets: ArrayList<Any>
		get() {
			val result = ArrayList<Any>()
			result.addAll(base.assets.assetsRefs)
			base.assets.assets.forEach {
				when(it) {
					is LowLevelModel -> result.add(Model(root, it))
					is LowLevelFill -> result.add(Fill(root, it))
					is LowLevelImage -> result.add(Image(root, it))
					is LowLevelLabel -> result.add(Label(root, it))
					is LowLevelText -> result.add(Text(root, it))
					else -> throw Exception("Unexpected ARAnchor Asset Type: $it")
				}
			}
			return result
		}

	override val elementsById: HashMap<String, ARElement>
		get() {
			val result: HashMap<String, ARElement> = HashMap()
			assets.forEach {
				when(it) {
					is Model -> { it.id?.let { id -> result[id] = it}; result.putAll(it.elementsById) }
					is Fill -> { it.id?.let { id -> result[id] = it}; result.putAll(it.elementsById) }
					is Image -> { it.id?.let { id -> result[id] = it}; result.putAll(it.elementsById) }
					is Label -> { it.id?.let { id -> result[id] = it}; result.putAll(it.elementsById) }
					is Text -> { it.id?.let { id -> result[id] = it}; result.putAll(it.elementsById) }
					else -> throw Exception("Unexpected ARAnchor Asset Type: $it")
				}
			}
			return result
		}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,assets=$assets)"
	}

	override fun validate(): Pair<Boolean, String> {
		val result = super.validate(); if (!result.first) return result
		assets.filterIsInstance(VisualAsset::class.java).forEach { val result1 = it.validate(); if (!result1.first) return result1 }
		return Pair(true, "Success")
	}
}




//REQ: http://www.opengis.net/spec/arml/2.0/req/model/ARAnchor/interface
internal abstract class LowLevelARAnchor : LowLevelAnchor() {

	@field:Element(name = "assets", required = true)
	lateinit var assets: ARAnchorAssets

	@Root(name = "assets", strict = true)
	internal class ARAnchorAssets {

		//REQ: http://www.opengis.net/spec/arml/2.0/req/model/ARAnchor/relative
		@field:ElementList(name = "assetRef", type = AssetRef::class, inline = true, required = false)
		var assetsRefs: List<AssetRef> = ArrayList()

		@Root(name = "assetRef", strict = true)
		internal class AssetRef {
			@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
			@field:Attribute(name = "href", required = true)
			lateinit var href: String
		}

		@field:ElementListUnion(
			// VisualAssets
			ElementList(name = "Model", type = LowLevelModel::class, inline = true, required = false),
			// VisualAssets2D
			ElementList(name = "Fill", type = LowLevelFill::class, inline = true, required = false),
			ElementList(name = "Image", type = LowLevelImage::class, inline = true, required = false),
			ElementList(name = "Label", type = LowLevelLabel::class, inline = true, required = false),
			ElementList(name = "Text", type = LowLevelText::class, inline = true, required = false),
		)
		var assets: List<LowLevelVisualAsset> = ArrayList()
	}
}
