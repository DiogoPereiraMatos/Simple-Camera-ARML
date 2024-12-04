package com.simplemobiletools.camera.ar.arml.elements.gml

import com.simplemobiletools.camera.ar.arml.elements.ARML
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class LineString : GMLGeometry {
	var posList: String

	constructor(id: String, posList: String) : super(id) {
		this.posList = posList
	}

	constructor(other: LineString) : super(other) {
		this.posList = other.posList
	}

	override fun toString(): String {
		return "${this::class.simpleName}(posList=$posList)"
	}


	internal constructor(root: ARML, base: LowLevelLineString) : this(base.id, base.posList)
}


@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "LineString")
internal class LowLevelLineString : LowLevelGMLGeometry() {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Element(name = "posList", required = true)
	var posList: String = ""
		get() = field.trimIndent().filterNot { it == '\n' }
}
