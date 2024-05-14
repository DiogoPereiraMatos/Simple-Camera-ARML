package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class Tracker internal constructor(
	private val root: ARML,
	private val base: LowLevelTracker
) : ARElement(root, base) {

	internal constructor(root: ARML, other: Tracker) : this(root, other.base)

	val uri: String = base.uri.href
	val src: String? = base.src?.href

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun toString(): String {
		return "Tracker(id=\"$id\",uri=\"$uri\",src=\"$src\")"
	}

	override fun validate(): Pair<Boolean, String> {
		return Pair(true, "Success")
	}
}




@Root(name = "Tracker", strict = true)
internal class LowLevelTracker : LowLevelARElement() {

	@field:Element(name = "uri", required = true)
	lateinit var uri: URI

	@Root(name = "uri", strict = true)
	class URI {
		@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
		@field:Attribute(name = "href", required = true)
		lateinit var href: String
	}

	@field:Element(name = "src", required = false)
	var src: SRC? = null

	@Root(name = "src", strict = true)
	class SRC {
		@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
		@field:Attribute(name = "href", required = true)
		lateinit var href: String
	}
}
