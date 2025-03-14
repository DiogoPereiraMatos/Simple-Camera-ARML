package com.simplemobiletools.camera.ar.arml.elements

import com.simplemobiletools.camera.ar.arml.elements.gml.*
import org.simpleframework.xml.*

interface RelativeToAble

class RelativeTo : ARAnchor {
	override val arElementType: ARElementType = ARElementType.RELATIVETO

	var ref: String
	var geometry: GMLGeometry

	constructor(ref: String, geometry: GMLGeometry) : super() {
		this.ref = ref
		this.geometry = geometry
	}

	constructor(other: RelativeTo) : super(other) {
		this.ref = other.ref
		this.geometry = other.geometry
	}

	override fun validate(): Pair<Boolean, String> {
		super.validate().let { if (!it.first) return it }
		geometry.validate().let { if (!it.first) return it }
		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelRelativeTo) : super(root, base) {
		this.ref = base.ref.href
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


@Root(name = "RelativeTo", strict = true)
internal class LowLevelRelativeTo : LowLevelARAnchor() {

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
	lateinit var geometry: LowLevelGMLGeometry
}
