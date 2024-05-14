package com.simplemobiletools.camera.ar.arml.elements

import com.simplemobiletools.camera.ar.arml.elements.gml.GMLGeometries
import com.simplemobiletools.camera.ar.arml.elements.gml.LineString
import com.simplemobiletools.camera.ar.arml.elements.gml.Point
import com.simplemobiletools.camera.ar.arml.elements.gml.Polygon
import com.simplemobiletools.camera.extensions.getRandomMediaName
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementUnion
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class Geometry internal constructor(
	private val root: ARML,
	private val base: LowLevelGeometry
) : ARAnchor(root, base) {

	internal constructor(root: ARML, other: Geometry) : this(root, other.base)

	val geometry: GMLGeometries = base.geometry

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
		Element(name = "Point", required = false, type = Point::class),
		Element(name = "LineString", required = false, type = LineString::class),
		Element(name = "Polygon", required = false, type = Polygon::class),
	)
	lateinit var geometry: GMLGeometries
}
