package com.simplemobiletools.camera.ar.arml.elements.gml

import com.simplemobiletools.camera.ar.arml.elements.ARML
import com.simplemobiletools.camera.ar.arml.elements.PrintableElement
import com.simplemobiletools.camera.ar.arml.elements.SUCCESS
import com.simplemobiletools.camera.ar.arml.elements.replaceAllWith
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class Polygon : GMLGeometry {
	var exterior: LinearRing
	val interior: ArrayList<LinearRing> = ArrayList()

	constructor(other: Polygon) : super(other.id) {
		this.exterior = other.exterior
		this.interior.replaceAllWith(other.interior)
	}

	override fun validate(): Pair<Boolean, String> {
		super.validate().let { if (!it.first) return it }
		exterior.validate().let { if (!it.first) return it }
		interior.forEach { linearRing -> linearRing.validate().let { if (!it.first) return it } }
		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelPolygon) : super(root, base) {
		this.exterior = LinearRing(root, base.exterior)
		this.interior.replaceAllWith(base.interior.map { LinearRing(root, it) })
	}
}


@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "Polygon")
internal class LowLevelPolygon : LowLevelGMLGeometry() {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Element(name = "exterior", required = true)
	lateinit var exterior: LowLevelLinearRing

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:ElementList(name = "interior", required = false, inline = true)
	var interior: List<LowLevelLinearRing> = ArrayList()
}






class LinearRing: PrintableElement {
	val posList: ArrayList<Double> = ArrayList()

	constructor(posList: List<Double>) {
		this.posList.replaceAllWith(posList)
	}

	constructor(other: LineString) {
		this.posList.replaceAllWith(other.posList)
	}

	fun validate(): Pair<Boolean, String> {
		if (posList.size < 4) return Pair(false, "LinearRing must have at least 4 points.")
		//if (posList.first() != posList.last()) return Pair(false, "LinearRing must be closed.")
		if (posList.subList(0,1) != posList.subList(posList.size-2,posList.size-1)) return Pair(false, "LinearRing must be closed.")
		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelLinearRing) {
		this.posList.replaceAllWith(base.posList.split(' ').map { it.toDouble() })
	}
}


@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "LinearRing")
internal class LowLevelLinearRing {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Element(name = "posList", required = true)
	var posList: String = ""
		get() = field.trimIndent()
}
