package com.simplemobiletools.camera.ar.armlparser.elements

import com.example.armlparser.elements.HREF
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "Image")
class Image : ARElement() {

	@field:Element(name = "href")
	lateinit var href : HREF

	override fun toString(): String {
		return "Image(id=$id,href=$href)"
	}
}
