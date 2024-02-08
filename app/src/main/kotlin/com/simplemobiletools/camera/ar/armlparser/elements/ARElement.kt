package com.simplemobiletools.camera.ar.armlparser.elements

import org.simpleframework.xml.Attribute


abstract class ARElement {

	@field:Attribute(name = "id")
	lateinit var id : String

	override fun toString(): String {
		return "ARElement(id=$id)"
	}
}
