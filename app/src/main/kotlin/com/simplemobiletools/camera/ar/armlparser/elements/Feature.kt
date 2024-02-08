package com.example.armlparser.elements

import com.simplemobiletools.camera.ar.armlparser.elements.ARElement
import com.simplemobiletools.camera.ar.armlparser.elements.Anchors
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "Feature")
class Feature : ARElement() {

	@field:Element(name = "name")
	lateinit var name : String

	@field:ElementList(name = "metadata")
	lateinit var metadata : List<String>

	@field:Element(name = "anchors")
	lateinit var anchors : Anchors

	override fun toString(): String {
		return "Trackable(id=$id,name=$name,metadata=$metadata,anchors=$anchors)"
	}
}
