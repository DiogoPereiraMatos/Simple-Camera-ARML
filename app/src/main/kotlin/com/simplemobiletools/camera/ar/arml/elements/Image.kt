package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class Image : VisualAsset2D {
	override val arElementType = ARElementType.IMAGE

	var href: String

	constructor(href: String) : super() {
		this.href = href
	}

	constructor(other: Image) : super(other) {
		this.href = other.href
	}

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun validate(): Pair<Boolean, String> {
		super.validate().let { if (!it.first) return it }
		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelImage) : super(root, base) {
		this.href = base.href.href
	}
}


@Root(name = "Image", strict = true)
internal class LowLevelImage : LowLevelVisualAsset2D() {

	@field:Element(name = "href", required = true)
	lateinit var href: HREF

	@Root(name = "href")
	class HREF {
		@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
		@field:Attribute(name = "href", required = true)
		lateinit var href: String
	}
}
