package com.simplemobiletools.camera.ar.armlparser.elements

import com.example.armlparser.elements.HREF
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "Tracker")
class Tracker : ARElement() {

	@field:Element(name = "uri")
	lateinit var uri : HREF

	override fun toString(): String {
		return "Tracker(id=$id,uri=$uri)"
	}
}
