package com.simplemobiletools.camera.ar.arml.elements.gml

import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "exterior")
class PolygonExterior {

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
