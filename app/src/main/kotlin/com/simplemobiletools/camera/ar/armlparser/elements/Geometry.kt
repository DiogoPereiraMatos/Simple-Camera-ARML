package com.simplemobiletools.camera.ar.armlparser.elements

import com.simplemobiletools.camera.ar.armlparser.elements.gml.GMLGeometries
import com.simplemobiletools.camera.ar.armlparser.elements.gml.LineString
import com.simplemobiletools.camera.ar.armlparser.elements.gml.Point
import com.simplemobiletools.camera.ar.armlparser.elements.gml.Polygon
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementUnion
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

@Root(name = "Geometry", strict = true)
class Geometry : ARAnchor() {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:ElementUnion(
		Element(name = "Point", required = false, type = Point::class),
		Element(name = "LineString", required = false, type = LineString::class),
		Element(name = "Polygon", required = false, type = Polygon::class),
	)
	lateinit var geometry: GMLGeometries

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,assets=$assets,geometry=$geometry)"
	}

	override fun validate(): Pair<Boolean, String> {
		val result = super.validate(); if (!result.first) return result
		val result1 = geometry.validate(); if (!result1.first) return result1
		return Pair(true, "Success")
	}
}
