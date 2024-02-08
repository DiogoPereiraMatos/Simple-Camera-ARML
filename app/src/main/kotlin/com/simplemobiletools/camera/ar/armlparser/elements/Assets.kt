package com.example.armlparser.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "assets")
class Assets {

	@field:Element(name = "assetRef")
	lateinit var assetRef : HREF

	override fun toString(): String {
		return "Assets(assetRef=$assetRef)"
	}
}
