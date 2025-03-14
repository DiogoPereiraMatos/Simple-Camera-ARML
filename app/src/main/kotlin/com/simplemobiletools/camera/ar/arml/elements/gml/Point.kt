package com.simplemobiletools.camera.ar.arml.elements.gml

import com.simplemobiletools.camera.ar.arml.elements.ARML
import com.simplemobiletools.camera.ar.arml.elements.SUCCESS
import com.simplemobiletools.camera.ar.arml.elements.replaceAllWith
import dev.romainguy.kotlin.math.Float3
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class Point : GMLGeometry {
	val pos: ArrayList<Double> = ArrayList()

	val asVec3: Float3 get() = Float3(pos[0].toFloat(), pos[1].toFloat(), pos[2].toFloat())

	private constructor(id: String) : super(id)

	constructor(id: String, pos: List<Double>) : super(id) {
		this.pos.replaceAllWith(pos)
	}

	constructor(other: Point) : super(other) {
		this.pos.replaceAllWith(other.pos)
	}

	override fun validate(): Pair<Boolean, String> {
		if (pos.size != srsDimension)
			return Pair(
				false,
				"Dimension of \"pos\" element in ${this::class.simpleName} is different from value of \"srsDimension\" attribute, got \"pos\"=$pos, \"srsDimension=$srsDimension\""
			)
		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelPoint) : super(root, base) {
		this.pos.replaceAllWith(base.pos.split(' ').map { it.toDouble() })
	}
}


@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "Point")
internal class LowLevelPoint : LowLevelGMLGeometry() {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Element(name = "pos", required = true)
	var pos: String = ""
		get() = field.trimIndent()
}
