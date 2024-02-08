package com.simplemobiletools.camera.ar.armlparser.elements

import com.example.armlparser.elements.HREF
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "anchors")
class Anchors {

	@field:Element(name = "anchorRef")
	lateinit var anchorRef : HREF

	override fun toString(): String {
		return "Anchor(anchorRef=$anchorRef)"
	}
}
