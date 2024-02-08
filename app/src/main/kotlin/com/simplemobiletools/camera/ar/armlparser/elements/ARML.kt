package com.simplemobiletools.camera.ar.armlparser.elements

import com.example.armlparser.elements.*
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementListUnion
import org.simpleframework.xml.Root

@Root(name = "ARML", strict = true)
class ARML {

	@field:ElementListUnion(
		ElementList(name = "Image", inline = true, type = Image::class),
		ElementList(name = "Tracker", inline = true, type = Tracker::class),
		ElementList(name = "Trackable", inline = true, type = Trackable::class),
		ElementList(name = "Feature", inline = true, type = Feature::class)
	)
	lateinit var elements: List<ARElement>

	override fun toString(): String {
		return "ARML(ARElements=$elements)"
	}
}
