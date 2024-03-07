package com.simplemobiletools.camera.ar.armlparser.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

@Root(name = "style", strict = true)
class Style {

	@field:Attribute(name = "type", required = false)
	var type: String? = null

	@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
	@field:Attribute(name = "href", required = false)
	var href: String? = null

	@field:Text
	var content: String = ""
		get() = field.trimIndent().filterNot { it == '\n' }

	override fun toString(): String {
		return "style(type=\"$type\",href=\"$href\",content=\"$content\")"
	}

	fun validate(): Pair<Boolean, String> {
		return Pair(true, "Success")
	}
}
