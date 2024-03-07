package com.simplemobiletools.camera.ar.armlparser.elements

import org.simpleframework.xml.Element

//REQ: http://www.opengis.net/spec/arml/2.0/req/model/Anchor/interface
abstract class Anchor : ARElement() {

	@field:Element(name = "enabled", required = false)
	var enabled: Boolean? = true

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled)"
	}

	override fun validate(): Pair<Boolean, String> {
		return Pair(true, "Success")
	}
}
