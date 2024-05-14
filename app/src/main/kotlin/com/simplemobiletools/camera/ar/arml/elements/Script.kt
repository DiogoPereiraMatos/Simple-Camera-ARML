package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

class Script internal constructor(
	private val root: ARML,
	private val base: LowLevelScript
) {

	internal constructor(root: ARML, other: Script) : this(root, other.base)

	val type: String? = base.type
	val href: String? = base.href
	val content: String = base.content

	override fun toString(): String {
		return "script(type=\"$type\",href=\"$href\",content=\"$content\")"
	}

	fun validate(): Pair<Boolean, String> {
		return Pair(true, "Success")
	}
}




@Root(name = "script", strict = true)
internal class LowLevelScript {

	@field:Attribute(name = "type", required = false)
	var type: String? = null

	@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
	@field:Attribute(name = "href", required = false)
	var href: String? = null

	@field:Text
	var content: String = ""
		get() = field.trimIndent().filterNot { it == '\n' }
}
