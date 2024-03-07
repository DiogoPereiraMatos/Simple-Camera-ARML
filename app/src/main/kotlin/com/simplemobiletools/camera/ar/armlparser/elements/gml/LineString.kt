package com.simplemobiletools.camera.ar.armlparser.elements.gml

import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "LineString")
class LineString : GMLGeometries() {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Element(name = "posList", required = true)
	var posList: String = ""
		get() = field.trimIndent().filterNot { it == '\n' }

	override fun toString(): String {
		return "${this::class.simpleName}(posList=$posList)"
	}

	override fun validate(): Pair<Boolean, String> {
		return Pair(true, "Success")
	}
}
