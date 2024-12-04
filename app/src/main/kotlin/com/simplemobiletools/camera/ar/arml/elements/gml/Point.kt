package com.simplemobiletools.camera.ar.arml.elements.gml

import com.simplemobiletools.camera.ar.arml.elements.ARML
import com.simplemobiletools.camera.ar.arml.elements.SUCCESS
import com.simplemobiletools.camera.ar.arml.elements.replaceAllWith
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class Point : GMLGeometry {
	val pos: ArrayList<Float> = ArrayList()
	var srsDimension: Int

	constructor(id: String, srsDimension: Int) : super(id) {
		this.srsDimension = srsDimension
	}

	constructor(other: Point) : super(other) {
		this.pos.replaceAllWith(other.pos)
		this.srsDimension = other.srsDimension
	}

	override fun validate(): Pair<Boolean, String> {
		if (pos.size != srsDimension)
			return Pair(
				false,
				"Dimension of \"pos\" element in ${this::class.simpleName} is different from value of \"srsDimension\" attribute, got \"pos\"=$pos, \"srsDimension=$srsDimension\""
			)
		return SUCCESS
	}

	override fun toString(): String {
		return "${this::class.simpleName}(pos=\"$pos\")"
	}


	internal constructor(root: ARML, base: LowLevelPoint) : this(base.id, base.srsDimension) {
		this.pos.replaceAllWith(base.pos.split(' ').map { it.toFloat() })
	}
}


@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "Point")
internal class LowLevelPoint : LowLevelGMLGeometry() {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Element(name = "pos", required = true)
	var pos: String = ""
		get() = field.trimIndent().filterNot { it == '\n' }

	@field:Attribute(name = "srsDimension", required = false)
	var srsDimension: Int = 2
}
