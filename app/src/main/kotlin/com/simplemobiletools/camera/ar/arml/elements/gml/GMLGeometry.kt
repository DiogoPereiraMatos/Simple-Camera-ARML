package com.simplemobiletools.camera.ar.arml.elements.gml

import com.simplemobiletools.camera.ar.arml.elements.ARML
import com.simplemobiletools.camera.ar.arml.elements.SUCCESS
import org.simpleframework.xml.Attribute

abstract class GMLGeometry {
	var id: String
	var srsName: String? = null
	var srsDimention: Int? = null

	constructor(id: String) {
		this.id = id
	}

	constructor(other: GMLGeometry) : this(other.id) {
		this.srsName = other.srsName
		this.srsDimention = other.srsDimention
	}

	open fun validate(): Pair<Boolean, String> {
		if (srsDimention != null && srsDimention!! <= 0) {
			return Pair(
				false,
				"srsDimention must be positive."
			)
		}
		return SUCCESS
	}

	override fun toString(): String {
		return "${this::class.simpleName}(gml:id=$id)"
	}


	internal constructor(root: ARML, base: LowLevelGMLGeometry) : this(base.id) {
		this.srsName = null
		this.srsDimention = null
	}
}


internal abstract class LowLevelGMLGeometry {

	//@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Attribute(name = "id", required = true)
	lateinit var id: String
}
