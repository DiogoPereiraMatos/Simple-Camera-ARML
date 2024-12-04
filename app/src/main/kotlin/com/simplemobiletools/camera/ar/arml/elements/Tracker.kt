package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class Tracker : ARElement {
	override val arElementType = ARElementType.TRACKER

	var uri: String
	var src: String? = null

	constructor(uri: String) : super() {
		this.uri = uri
	}

	constructor(other: Tracker) : super(other) {
		this.uri = other.uri
		this.src = other.src
	}

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun validate(): Pair<Boolean, String> = SUCCESS

	override fun toString(): String {
		return "Tracker(id=\"$id\",uri=\"$uri\",src=\"$src\")"
	}


	internal constructor(root: ARML, base: LowLevelTracker) : super(base) {
		this.uri = base.uri.href
		this.src = base.src?.href
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
