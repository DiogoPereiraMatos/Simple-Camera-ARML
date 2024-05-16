package com.simplemobiletools.camera.ar.arml.elements.gml

import com.simplemobiletools.camera.ar.arml.elements.ARML
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class Point internal constructor(
	private val root: ARML,
	private val base: LowLevelPoint
) : GMLGeometries(root, base) {

	internal constructor(root: ARML, other: Point) : this(root, other.base)

	val pos : List<Float> = base.pos.split(' ').map { it.toFloat() }
	var srsDimension: Int = base.srsDimension

	override fun toString(): String {
		return "${this::class.simpleName}(pos=\"$pos\")"
	}

	override fun validate(): Pair<Boolean, String> {
		if (pos.size != srsDimension) return Pair(
			false,
			"Dimension of \"pos\" element in ${this::class.simpleName} is different from value of \"srsDimension\" attribute, got \"pos\"=$pos, \"srsDimension=$srsDimension\""
		)
		return Pair(true, "Success")
	}
}




@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "Point")
internal class LowLevelPoint : LowLevelGMLGeometries() {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Element(name = "pos", required = true)
	var pos: String = ""
		get() = field.trimIndent().filterNot { it == '\n' }

	@field:Attribute(name = "srsDimension", required = false)
	var srsDimension: Int = 2
}
