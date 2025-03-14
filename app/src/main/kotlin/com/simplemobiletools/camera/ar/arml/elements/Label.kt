package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

enum class HyperlinkBehaviour {
	BLOCK,
	BLANK,
	SELF;

	override fun toString(): String {
		return this.name.lowercase()
	}
}

class Label : VisualAsset2D {
	override val arElementType = ARElementType.LABEL

	var href: String? = null
	var src: String? = null
	var hyperlinkBehavior: HyperlinkBehaviour = HyperlinkBehaviour.BLANK
	var viewportWidth: Int = 256
		get() = if (field < 0) 256 else field

	constructor() : super()

	constructor(other: Label) : super(other) {
		this.href = other.href
		this.src = other.src
		this.hyperlinkBehavior = other.hyperlinkBehavior
		this.viewportWidth = other.viewportWidth
	}

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun validate(): Pair<Boolean, String> {
		super.validate().let { if (!it.first) return it }

		if (href == null && src == null)
			return Pair(
				false,
				"At least one of \"href\" and \"src\" must be set on $this."
			)

		if (viewportWidth <= 0)
			return Pair(
				false,
				"\"viewportWidth\" element in ${this::class.simpleName} must be positive, got $viewportWidth"
			)

		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelLabel) : super(root, base) {
		this.href = base.href?.href
		this.src = base.src
		this.viewportWidth = base.viewportWidth ?: 256

		if (base.hyperlinkBehavior != null) {
			try {
				this.hyperlinkBehavior = HyperlinkBehaviour.valueOf(base.hyperlinkBehavior!!.uppercase())
			} catch (e: IllegalArgumentException) {
				val possibleValues = HyperlinkBehaviour.entries.map { it.toString() }
				throw Exception("Expected one of $possibleValues for \"hyperlinkBehavior\" element in ${this::class.simpleName}, got \"${base.hyperlinkBehavior}\"")
			}
		}
	}
}


@Root(name = "Label", strict = true)
internal class LowLevelLabel : LowLevelVisualAsset2D() {

	@field:Element(name = "href", required = false)
	var href: HREF? = null

	@Root(name = "href")
	class HREF {
		@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
		@field:Attribute(name = "href", required = true)
		lateinit var href: String
	}

	//TODO: Any Type
	@field:Element(name = "src", required = false)
	var src: String? = null

	@field:Element(name = "hyperlinkBehavior", required = false)
	var hyperlinkBehavior: String? = null

	@field:Element(name = "viewportWidth", required = false)
	var viewportWidth: Int? = null
}
