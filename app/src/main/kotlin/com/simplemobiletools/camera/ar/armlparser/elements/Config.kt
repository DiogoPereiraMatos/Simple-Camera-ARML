package com.example.armlparser.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "config")
class Config {

	@field:Element(name = "tracker")
	lateinit var tracker : HREF

	override fun toString(): String {
		return "Config(tracker=$tracker)"
	}
}