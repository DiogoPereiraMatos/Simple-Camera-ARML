package com.simplemobiletools.camera.ar.arml.elements

import com.simplemobiletools.camera.ar.arml.elements.gml.*
import com.simplemobiletools.camera.ar.arml.elements.gml.LowLevelGMLGeometries
import com.simplemobiletools.camera.ar.arml.elements.gml.LowLevelPoint
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementUnion
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class RelativeTo internal constructor(
	private val root: ARML,
	private val base: LowLevelRelativeTo
) : ARAnchor(root, base) {

	internal constructor(root: ARML, other: RelativeTo) : this(root, other.base)

	val ref: String = base.ref.href
	val geometry: GMLGeometries = base.geometry.let {
		when (it) {
			is LowLevelPoint -> Point(root, it)
			is LowLevelLineString -> LineString(root, it)
			is LowLevelPolygon -> Polygon(root, it)
			else -> throw Exception("Unexpected Geometry GMLGeometries Type: $it")
		}
	}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,assets=$assets,ref=\"$ref\",geometry=$geometry)"
	}

	override fun validate(): Pair<Boolean, String> {
		val result = super.validate(); if (!result.first) return result
		val result1 = geometry.validate(); if (!result1.first) return result1
		return Pair(true, "Success")
	}
}




@Root(name = "RelativeTo", strict = true)
internal class LowLevelRelativeTo : LowLevelARAnchor() {

	//REQ: http://www.opengis.net/spec/arml/2.0/req/model/RelativeTo/ref
	@field:Element(name = "ref", required = true)
	lateinit var ref: REF

	@Root(name = "ref", strict = true)
	internal class REF {
		@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
		@field:Attribute(name = "href", required = true)
		lateinit var href: String
	}

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:ElementUnion(
		Element(name = "Point", required = false, type = LowLevelPoint::class),
		Element(name = "LineString", required = false, type = LowLevelLineString::class),
		Element(name = "Polygon", required = false, type = LowLevelPolygon::class)
	)
	lateinit var geometry: LowLevelGMLGeometries
}
