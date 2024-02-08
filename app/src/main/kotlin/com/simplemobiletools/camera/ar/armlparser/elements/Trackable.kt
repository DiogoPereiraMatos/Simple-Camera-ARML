package com.simplemobiletools.camera.ar.armlparser.elements

import com.example.armlparser.elements.Assets
import com.example.armlparser.elements.Config
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "Trackable")
class Trackable : ARElement() {

	@field:Element(name = "config")
	lateinit var config : Config

	@field:Element(name = "assets")
	lateinit var assets : Assets

	override fun toString(): String {
		return "Trackable(id=$id,config=$config,assets=$assets)"
	}
}
