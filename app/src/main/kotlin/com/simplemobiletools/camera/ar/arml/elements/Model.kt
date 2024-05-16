package com.simplemobiletools.camera.ar.arml.elements

import dev.romainguy.kotlin.math.Float3
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class Model internal constructor(
	private val root: ARML,
	private val base: LowLevelModel
) : VisualAsset(root, base) {

	internal constructor(root: ARML, other: Model) : this(root, other.base)

	val href: String = base.href.href
	val type: String? = base.type
	val scale: Scale? = base.scale?.let { Scale(root, it) }

	val scaleVector: Float3 = Float3(
		scale?.x?.toFloat() ?: 1f,
		scale?.y?.toFloat() ?: 1f,
		scale?.z?.toFloat() ?: 1f,
	)

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun validate(): Pair<Boolean, String> {
		val result = super.validate(); if (!result.first) return result
		if (type != null) {
			if (type.lowercase() !in arrayOf("normal", "infrastructure")) return Pair(
				false,
				"Expected \"normal\" or \"infrastructure\" for \"type\" element in ${this::class.simpleName}, got \"$type\""
			)
		}
		if (scale != null) {
			val result1 = scale.validate()
			if (!result1.first) return result1
		}
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,zOrder=$zOrder,orientation=$orientation,scalingMode=$scalingMode,conditions=$conditions,href=\"$href\",type=\"$type\",scale=$scale)"
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
