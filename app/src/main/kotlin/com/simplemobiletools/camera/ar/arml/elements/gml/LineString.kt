package com.simplemobiletools.camera.ar.arml.elements.gml

import com.simplemobiletools.camera.ar.arml.elements.ARML
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class LineString internal constructor(
	private val root: ARML,
	private val base: LowLevelLineString
) : GMLGeometries(root, base) {

	internal constructor(root: ARML, other: LineString) : this(root, other.base)

	val posList : String = base.posList

	override fun toString(): String {
		return "${this::class.simpleName}(posList=$posList)"
	}

	override fun validate(): Pair<Boolean, String> {
		return Pair(true, "Success")
	}
}




@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
@Root(name = "LineString")
internal class LowLevelLineString : LowLevelGMLGeometries() {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Element(name = "posList", required = true)
	var posList: String = ""
		get() = field.trimIndent().filterNot { it == '\n' }
}
