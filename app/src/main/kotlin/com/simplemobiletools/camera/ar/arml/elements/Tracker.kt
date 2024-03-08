package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

@Root(name = "Tracker", strict = true)
class Tracker : ARElement() {

	@field:Element(name = "uri", required = true)
	lateinit var uri: URI

	@Root(name = "uri", strict = true)
	class URI {
		@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
		@field:Attribute(name = "href", required = true)
		lateinit var href: String

		override fun toString(): String {
			return href
		}
	}

	@field:Element(name = "src", required = false)
	var src: SRC? = null

	@Root(name = "src", strict = true)
	class SRC {
		@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
		@field:Attribute(name = "href", required = true)
		lateinit var href: String

		override fun toString(): String {
			return href
		}
	}

	override fun toString(): String {
		return "Tracker(id=\"$id\",uri=\"$uri\",src=\"$src\")"
	}

	override fun validate(): Pair<Boolean, String> {
		return Pair(true, "Success")
	}
}
