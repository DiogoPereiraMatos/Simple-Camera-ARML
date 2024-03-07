package com.simplemobiletools.camera.ar.armlparser.elements.gml

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "Polygon")
class Polygon : GMLGeometries() {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Element(name = "exterior", required = true)
	lateinit var exterior: PolygonExterior

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:ElementList(name = "interior", required = false, inline = true)
	var interior: List<PolygonInterior> = ArrayList()

	override fun toString(): String {
		return "${this::class.simpleName}(exterior=$exterior,interior=$interior)"
	}

	override fun validate(): Pair<Boolean, String> {
		return Pair(true, "Success")
	}
}
