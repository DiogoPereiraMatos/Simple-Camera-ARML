package com.simplemobiletools.camera.ar.arml.elements

import com.simplemobiletools.camera.ar.arml.elements.gml.*
import com.simplemobiletools.camera.ar.arml.elements.gml.LowLevelPoint
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementUnion
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class Geometry internal constructor(
	private val root: ARML,
	private val base: LowLevelGeometry
) : ARAnchor(root, base) {

	internal constructor(root: ARML, other: Geometry) : this(root, other.base)

	val geometry: GMLGeometries = base.geometry.let {
		when (it) {
			is LowLevelPoint -> Point(root, it)
			is LowLevelLineString -> LineString(root, it)
			is LowLevelPolygon -> Polygon(root, it)
			else -> throw Exception("Unexpected Geometry GMLGeometries Type: $it")
		}
	}

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,assets=$assets,geometry=$geometry)"
	}

	override fun validate(): Pair<Boolean, String> {
		val result = super.validate(); if (!result.first) return result
		val result1 = geometry.validate(); if (!result1.first) return result1
		return Pair(true, "Success")
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
	lateinit var geometry: LowLevelGMLGeometries
}
