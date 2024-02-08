package com.example.armlparser.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Root

@Root(name = "href")
class HREF {

	@field:Attribute(name = "href")
	lateinit var url : String

	override fun toString(): String {
		return "href(href=\"$url\")"
	}
}