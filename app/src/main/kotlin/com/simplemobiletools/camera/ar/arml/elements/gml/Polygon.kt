package com.simplemobiletools.camera.ar.arml.elements.gml

import com.simplemobiletools.camera.ar.arml.elements.ARML
import com.simplemobiletools.camera.ar.arml.elements.replaceAllWith
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class Polygon : GMLGeometry {
	var exterior: PolygonExterior
	val interior: ArrayList<PolygonInterior> = ArrayList()

	constructor(id: String, exterior: PolygonExterior) : super(id) {
		this.exterior = exterior
	}

	constructor(other: Polygon) : super(other.id) {
		this.exterior = other.exterior
		this.interior.replaceAllWith(other.interior)
	}

	override fun toString(): String {
		return "${this::class.simpleName}(exterior=$exterior,interior=$interior)"
	}


	internal constructor(root: ARML, base: LowLevelPolygon) : this(base.id, PolygonExterior(root, base.exterior)) {
		this.interior.replaceAllWith(base.interior.map { PolygonInterior(root, it) })
	}
}


@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "Polygon")
internal class LowLevelPolygon : LowLevelGMLGeometry() {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Element(name = "exterior", required = true)
	lateinit var exterior: LowLevelPolygonExterior

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:ElementList(name = "interior", required = false, inline = true)
	var interior: List<LowLevelPolygonInterior> = ArrayList()
}
