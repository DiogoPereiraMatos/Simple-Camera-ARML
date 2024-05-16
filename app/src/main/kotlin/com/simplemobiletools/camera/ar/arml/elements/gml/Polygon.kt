package com.simplemobiletools.camera.ar.arml.elements.gml

import com.simplemobiletools.camera.ar.arml.elements.ARML
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class Polygon internal constructor(
	private val root: ARML,
	private val base: LowLevelPolygon
) : GMLGeometries(root, base) {

	internal constructor(root: ARML, other: Polygon) : this(root, other.base)

	val exterior : PolygonExterior = PolygonExterior(root, base.exterior)
	var interior: List<PolygonInterior> = base.interior.map { PolygonInterior(root, it) }

	override fun toString(): String {
		return "${this::class.simpleName}(exterior=$exterior,interior=$interior)"
	}

	override fun validate(): Pair<Boolean, String> {
		return Pair(true, "Success")
	}
}




@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "Polygon")
internal class LowLevelPolygon : LowLevelGMLGeometries() {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Element(name = "exterior", required = true)
	lateinit var exterior: LowLevelPolygonExterior

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:ElementList(name = "interior", required = false, inline = true)
	var interior: List<LowLevelPolygonInterior> = ArrayList()
}
