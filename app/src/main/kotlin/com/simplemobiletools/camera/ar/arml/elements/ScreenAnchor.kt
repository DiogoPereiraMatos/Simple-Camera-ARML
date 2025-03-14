package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.*

class ScreenAnchor : Anchor {
	override val arElementType: ARElementType = ARElementType.SCREENANCHOR

	var style: String? = null
	var css: String? = null
	val assets: ArrayList<Label> = ArrayList()

	constructor() : super()

	constructor(other: ScreenAnchor) : super(other) {
		this.style = other.style
		this.css = other.css
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


	internal constructor(root: ARML, base: LowLevelScreenAnchor) : super(base) {
		this.style = base.style
		this.css = base.css

		val result = ArrayList<Label>()
		base.assets.labels?.forEach {
			when (it) {
				is LowLevelLabel -> result.add(Label(root, it))
				else -> throw Exception("Unexpected ScreenAnchor Asset Type: $it")
			}
		}
		base.assets.assetsRefs?.forEach {
			val referred = root.elementsById[it.href] ?: throw Exception("Reference to unknown element: ${it.href}")
			if (referred !is Label) throw Exception("${it.href} Expected reference to asset but got: $referred")
			result.add(referred)
		}
		this.assets.replaceAllWith(result)
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
		var assetsRefs: List<AssetRef>? = null

		@Root(name = "assetRef", strict = true)
		class AssetRef {
			@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
			@field:Attribute(name = "href", required = true)
			lateinit var href: String
		}

		@field:ElementList(name = "Label", type = LowLevelLabel::class, inline = true, required = false)
		var labels: List<LowLevelVisualAsset>? = null
	}
}
