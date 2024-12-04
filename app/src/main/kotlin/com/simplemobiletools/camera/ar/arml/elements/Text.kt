package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

class Text : VisualAsset2D {
	override val arElementType = ARElementType.TEXT

	var src: String
	var style: String? = null
	var css: String? = null

	constructor(src: String) : super() {
		this.src = src
	}

	constructor(other: Text) : super(other) {
		this.src = other.src
		this.style = other.style
		this.css = other.css
	}

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun validate(): Pair<Boolean, String> {
		super.validate().let { if (!it.first) return it }
		return SUCCESS
	}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,zOrder=$zOrder,orientation=$orientation,scalingMode=$scalingMode,conditions=$conditions,width=\"$width\",height=\"$height\",orientationMode=\"$orientationMode\",backside=\"$backside\",src=\"$src\",style=\"$style\",class=\"$css\")"
	}


	internal constructor(root: ARML, base: LowLevelText) : super(root, base) {
		this.src = base.src
		this.style = base.style
		this.css = base.css
	}
}


@Root(name = "Text", strict = true)
internal class LowLevelText : LowLevelVisualAsset2D() {

	@field:Element(name = "src", required = true)
	lateinit var src: String

	@field:Element(name = "style", required = false)
	var style: String? = null

	@field:Element(name = "class", required = false)
	var css: String? = null
}
