package com.simplemobiletools.camera.ar.armlparser.elements.gml

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Namespace

//REQ: http://www.opengis.net/spec/arml/2.0/req/model/GMLGeometries/interface
abstract class GMLGeometries {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Attribute(name = "id", required = false)
	var id: String? = null

	override fun toString(): String {
		return "${this::class.simpleName}(gml:id=$id)"
	}

	abstract fun validate(): Pair<Boolean, String>
}
