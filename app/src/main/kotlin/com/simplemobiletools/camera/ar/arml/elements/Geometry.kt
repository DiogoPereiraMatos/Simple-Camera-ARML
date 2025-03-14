package com.simplemobiletools.camera.ar.arml.elements

import com.simplemobiletools.camera.ar.arml.elements.gml.*
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementUnion
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class Geometry : ARAnchor {
	override val arElementType: ARElementType = ARElementType.GEOMETRY

	var geometry: GMLGeometry

	constructor(geometry: GMLGeometry) : super() {
		this.geometry = geometry
	}

	constructor(other: Geometry) : super(other) {
		this.geometry = other.geometry
	}

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun validate(): Pair<Boolean, String> {
		super.validate().let { if (!it.first) return it }
		geometry.validate().let { if (!it.first) return it }
		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelGeometry) : super(root, base) {
		this.geometry = base.geometry.let {
			when (it) {
				is LowLevelPoint -> Point(root, it)
				is LowLevelLineString -> LineString(root, it)
				is LowLevelPolygon -> Polygon(root, it)
				else -> throw Exception("Unexpected Geometry GMLGeometries Type: $it")
			}
		}
	}
}


@Root(name = "Geometry", strict = true)
internal class LowLevelGeometry : LowLevelARAnchor() {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:ElementUnion(
		Element(name = "Point", required = false, type = LowLevelPoint::class),
		Element(name = "LineString", required = false, type = LowLevelLineString::class),
		Element(name = "Polygon", required = false, type = LowLevelPolygon::class),
	)
	lateinit var geometry: LowLevelGMLGeometry
}
