package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

class Style {
	var type: String? = null
	var href: String? = null
	var content: String

	constructor(content: String) : super() {
		this.content = content
	}

	constructor(other: Style) : this(other.content) {
		this.type = other.type
		this.href = other.href
	}

	fun validate(): Pair<Boolean, String> = SUCCESS

	override fun toString(): String {
		val content = this.content.filterNot { it == '\n' }.trimIndent().trim()
		return "style(type=\"$type\",href=\"$href\",content=\"$content\")"
	}


	internal constructor(root: ARML, base: LowLevelStyle) : this(base.content) {
		this.type = base.type
		this.href = base.href
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
	lateinit var content: String
}
