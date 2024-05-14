package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

class Style internal constructor(
	private val root: ARML,
	private val base: LowLevelStyle
) {

	internal constructor(root: ARML, other: Style) : this(root, other.base)

	val type: String? = base.type
	val href: String? = base.href
	val content: String = base.content

	override fun toString(): String {
		return "style(type=\"$type\",href=\"$href\",content=\"$content\")"
	}

	fun validate(): Pair<Boolean, String> {
		return Pair(true, "Success")
	}
}




@Root(name = "style", strict = true)
internal class LowLevelStyle {

	@field:Attribute(name = "type", required = false)
	var type: String? = null

	@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
	@field:Attribute(name = "href", required = false)
	var href: String? = null

	@field:Text
	var content: String = ""
		get() = field.trimIndent().filterNot { it == '\n' }
}
