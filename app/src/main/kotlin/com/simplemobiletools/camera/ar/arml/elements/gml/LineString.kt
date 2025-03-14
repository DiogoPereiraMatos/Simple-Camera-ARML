package com.simplemobiletools.camera.ar.arml.elements.gml

import com.simplemobiletools.camera.ar.arml.elements.ARML
import com.simplemobiletools.camera.ar.arml.elements.SUCCESS
import com.simplemobiletools.camera.ar.arml.elements.replaceAllWith
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class LineString : GMLGeometry {
	val posList: ArrayList<Double> = ArrayList()
	val pointProperty: ArrayList<Point> = ArrayList()

	private constructor(id: String) : super(id)

	constructor(id: String, posList: List<Double>, pointProperty: List<Point>) : super(id) {
		this.posList.replaceAllWith(posList)
		this.pointProperty.replaceAllWith(pointProperty)
	}

	constructor(other: LineString) : super(other) {
		this.posList.replaceAllWith(other.posList)
		this.pointProperty.replaceAllWith(other.pointProperty)
	}

	override fun validate(): Pair<Boolean, String> {
		super.validate().let { if (!it.first) return it }
		pointProperty.forEach { pointProperty -> pointProperty.validate().let { if (!it.first) return it } }
		if (pointProperty.size < 2) return Pair(false, "LineString must have at least 2 points.")
		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelLineString) : super(root, base) {
		this.posList.replaceAllWith(base.posList.split(' ').map { it.toDouble() })
		this.pointProperty.replaceAllWith(base.pointProperty.map { Point(root, it) })
	}
}


@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "LineString")
internal class LowLevelLineString : LowLevelGMLGeometry() {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Element(name = "posList", required = true)
	var posList: String = ""
		get() = field.trimIndent()

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:ElementList(name = "pointProperty", required = true, inline = true)
	var pointProperty: List<LowLevelPoint> = ArrayList()
}
