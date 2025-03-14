package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.*

abstract class ARAnchor : Anchor, RelativeToAble {
	var assets: ArrayList<VisualAsset> = ArrayList()
	val sortedAssets: ArrayList<VisualAsset>
		get() = ArrayList(assets.sortedBy { it.zOrder })

	constructor() : super()

	constructor(other: ARAnchor) : super(other) {
		this.assets.replaceAllWith(other.assets)
	}

	override val elementsById: HashMap<String, ARElement>
		get() {
			val result: HashMap<String, ARElement> = HashMap()
			assets.forEach {
				if (it.id != null)
					result[it.id!!] = it
				result.putAll(it.elementsById)
			}
			return result
		}

	override fun validate(): Pair<Boolean, String> {
		super.validate().let { if (!it.first) return it }
		assets.forEach { asset -> asset.validate().let { if (!it.first) return it } }
		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelARAnchor) : super(base) {
		val result = ArrayList<VisualAsset>()
		base.assets.assets?.forEach {
			when (it) {
				is LowLevelModel -> result.add(Model(root, it))
				is LowLevelFill -> result.add(Fill(root, it))
				is LowLevelImage -> result.add(Image(root, it))
				is LowLevelLabel -> result.add(Label(root, it))
				is LowLevelText -> result.add(Text(root, it))
				else -> throw Exception("Unexpected ARAnchor Asset Type: $it")
			}
		}
		base.assets.assetsRefs?.forEach {
			val referred = root.elementsById[it.href] ?: throw Exception("Reference to unknown element: ${it.href}")
			if (referred !is VisualAsset) throw Exception("${it.href} Expected reference to asset but got: $referred")
			result.add(referred)
		}
		this.assets.replaceAllWith(result)
	}
}


internal abstract class LowLevelARAnchor : LowLevelAnchor() {

	@field:Element(name = "assets", required = true)
	lateinit var assets: ARAnchorAssets

	@Root(name = "assets", strict = true)
	internal class ARAnchorAssets {

		@field:ElementList(name = "assetRef", type = AssetRef::class, inline = true, required = false)
		var assetsRefs: List<AssetRef>? = null

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
		var assets: List<LowLevelVisualAsset>? = null
	}
}
