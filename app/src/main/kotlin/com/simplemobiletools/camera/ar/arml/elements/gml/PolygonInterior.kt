package com.simplemobiletools.camera.ar.arml.elements.gml

import com.simplemobiletools.camera.ar.arml.elements.ARML
import com.simplemobiletools.camera.ar.arml.elements.SUCCESS
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class PolygonInterior {
	var ring: LinearRing

	constructor(ring: LinearRing) {
		this.ring = ring
	}

	constructor(other: PolygonInterior) : this(other.ring)

	fun validate(): Pair<Boolean, String> = SUCCESS

	override fun toString(): String {
		return "${this::class.simpleName}(ring=$ring)"
	}


	internal constructor(root: ARML, base: LowLevelPolygonInterior) : this(LinearRing(root, base.ring))
}


@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "interior")
internal class LowLevelPolygonInterior {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Element(name = "LinearRing", required = true)
	lateinit var ring: LowLevelLinearRing
}
