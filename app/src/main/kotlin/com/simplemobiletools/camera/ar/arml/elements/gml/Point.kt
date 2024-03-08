package com.simplemobiletools.camera.ar.arml.elements.gml

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "Point")
class Point : GMLGeometries() {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Element(name = "pos", required = true)
	var pos: String = ""
		get() = field.trimIndent().filterNot { it == '\n' }

	@field:Attribute(name = "srsDimension", required = false)
	var srsDimension: Int = 2

	override fun toString(): String {
		return "${this::class.simpleName}(pos=\"$pos\")"
	}

	override fun validate(): Pair<Boolean, String> {
		if (pos.split(' ').size != srsDimension) return Pair(
			false,
			"Dimension of \"pos\" element in ${this::class.simpleName} is different from value of \"srsDimension\" attribute, got \"pos\"=$pos, \"srsDimension=$srsDimension\""
		)
		return Pair(true, "Success")
	}
}
