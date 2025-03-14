package com.simplemobiletools.camera.ar.arml.elements.gml

import com.simplemobiletools.camera.ar.arml.elements.ARML
import com.simplemobiletools.camera.ar.arml.elements.PrintableElement
import com.simplemobiletools.camera.ar.arml.elements.SUCCESS
import org.simpleframework.xml.Attribute

abstract class GMLGeometry: PrintableElement {
	var id: String
	var srsName: String = "WGS84"
	var srsDimension: Int = 2

	constructor(id: String) {
		this.id = id
	}

	constructor(other: GMLGeometry) : this(other.id) {
		this.srsName = other.srsName
		this.srsDimension = other.srsDimension
	}

	open fun validate(): Pair<Boolean, String> {
		if (srsDimension <= 0) {
			return Pair(
				false,
				"srsDimension must be positive."
			)
		}
		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelGMLGeometry) : this(base.id) {
		this.srsName = base.srsName ?: this.srsName
		this.srsDimension = base.srsDimension ?: this.srsDimension
	}
}


internal abstract class LowLevelGMLGeometry {

	//@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Attribute(name = "id", required = true)
	lateinit var id: String

	@field:Attribute(name = "srsName", required = false)
	var srsName: String? = null

	@field:Attribute(name = "srsDimension", required = false)
	var srsDimension: Int? = null
}
