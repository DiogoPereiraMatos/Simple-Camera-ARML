package com.simplemobiletools.camera.ar.arml.elements

import dev.romainguy.kotlin.math.Float3
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

enum class ModelType {
	NORMAL,
	INFRASTRUCTURE;

	override fun toString(): String {
		return this.name.lowercase()
	}
}

class Model : VisualAsset, RelativeToAble {
	override val arElementType = ARElementType.MODEL

	var href: String
	var type: ModelType = ModelType.NORMAL
	var scale: Scale? = null

	val scaleVector: Float3
		get() = Float3(
			scale?.x?.toFloat() ?: 1f,
			scale?.y?.toFloat() ?: 1f,
			scale?.z?.toFloat() ?: 1f,
		)

	constructor(href: String) : super() {
		this.href = href
	}

	constructor(other: Model) : super(other) {
		this.href = other.href
		this.type = other.type
		this.scale = other.scale
	}

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun validate(): Pair<Boolean, String> {
		super.validate().let { if (!it.first) return it }
		scale?.let { scale -> scale.validate().let { if (!it.first) return it } }
		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelModel) : super(root, base) {
		this.href = base.href.href
		this.scale = base.scale?.let { Scale(root, it) }

		if (base.type != null) {
			try {
				this.type = ModelType.valueOf(base.type!!.uppercase())
			} catch (e: IllegalArgumentException) {
				val possibleValues = ModelType.entries.map { it.toString() }
				throw Exception("Expected one of $possibleValues for \"type\" element in ${this::class.simpleName}, got \"${base.type}\"")
			}
		}
	}
}


@Root(name = "Model", strict = true)
internal class LowLevelModel : LowLevelVisualAsset() {

	@field:Element(name = "href", required = true)
	lateinit var href: AssetRef

	@Root(name = "assetRef", strict = true)
	class AssetRef {
		@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
		@field:Attribute(name = "href", required = true)
		lateinit var href: String
	}

	@field:Element(name = "type", required = false)
	var type: String? = null

	@field:Element(name = "Scale", required = false)
	var scale: LowLevelScale? = null
}
