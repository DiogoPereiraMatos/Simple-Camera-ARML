package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

class Fill : VisualAsset2D {
	override val arElementType = ARElementType.FILL

	var style: String? = null
	var css: String? = null

	constructor() : super()

	constructor(other: Fill) : super(other) {
		this.style = other.style
		this.css = other.css
	}

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun validate(): Pair<Boolean, String> {
		super.validate().let { if (!it.first) return it }
		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelFill) : super(root, base) {
		this.style = base.style
		this.css = base.css
	}
}


@Root(name = "Fill", strict = true)
internal class LowLevelFill : LowLevelVisualAsset2D() {

	@field:Element(name = "style", required = false)
	var style: String? = null

	@field:Element(name = "class", required = false)
	var css: String? = null
}
