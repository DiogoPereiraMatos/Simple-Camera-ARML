package com.simplemobiletools.camera.ar.arml.elements.gml

import com.simplemobiletools.camera.ar.arml.elements.ARML
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class PolygonInterior internal constructor(
	private val root: ARML,
	private val base: LowLevelPolygonInterior
) {

	internal constructor(root: ARML, other: PolygonInterior) : this(root, other.base)

	val ring : LinearRing = LinearRing(root, base.ring)

	override fun toString(): String {
		return "${this::class.simpleName}(ring=$ring)"
	}

	fun validate(): Pair<Boolean, String> {
		return Pair(true, "Success")
	}
}




@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "interior")
internal class LowLevelPolygonInterior {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Element(name = "LinearRing", required = true)
	lateinit var ring: LowLevelLinearRing
}
