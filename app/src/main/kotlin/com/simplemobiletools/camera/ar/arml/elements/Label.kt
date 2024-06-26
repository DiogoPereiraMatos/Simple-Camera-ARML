package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class Label internal constructor(
	private val root: ARML,
	private val base: LowLevelLabel
) : VisualAsset2D(root, base) {

	internal constructor(root: ARML, other: Label) : this(root, other.base)

	val href: String? = base.href?.href
	val src: String? = base.src
	val hyperlinkBehavior: String? = base.hyperlinkBehavior
	val viewportWidth: Int? = base.viewportWidth

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun validate(): Pair<Boolean, String> {
		val result = super.validate(); if (!result.first) return result
		if (hyperlinkBehavior != null) if (hyperlinkBehavior.lowercase() !in arrayOf(
				"block",
				"blank",
				"self"
			)
		) return Pair(
			false,
			"Expected \"block\", \"blank\", or \"self\" for \"hyperlinkBehavior\" element in ${this::class.simpleName}, got \"$hyperlinkBehavior\""
		)
		if (viewportWidth != null) if (viewportWidth <= 0) return Pair(
			false,
			"\"viewportWidth\" element in ${this::class.simpleName} must be positive, got $viewportWidth"
		)
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,zOrder=$zOrder,orientation=$orientation,scalingMode=$scalingMode,conditions=$conditions,width=\"$width\",height=\"$height\",orientationMode=\"$orientationMode\",backside=\"$backside\",href=\"$href\",src=\"$src\",hyperlinkBehavior=\"$hyperlinkBehavior\",viewportWidth=$viewportWidth)"
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
