package com.simplemobiletools.camera.ar.armlparser.elements.gml

import com.simplemobiletools.camera.ar.armlparser.elements.gml.LinearRing
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "interior")
class PolygonInterior {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Element(name = "LinearRing", required = true)
	lateinit var ring: LinearRing

	override fun toString(): String {
		return "${this::class.simpleName}(ring=$ring)"
	}

	fun validate(): Pair<Boolean, String> {
		return Pair(true, "Success")
	}
}
