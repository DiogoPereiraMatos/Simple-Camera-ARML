package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute

//REQ: http://www.opengis.net/spec/arml/2.0/req/model/ARElement/interface
abstract class ARElement {

	//REQ: http://www.opengis.net/spec/arml/2.0/req/model/ARElement/id
	@field:Attribute(name = "id", required = false)
	var id: String? = null

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\")"
	}

	abstract fun validate(): Pair<Boolean, String>
}
