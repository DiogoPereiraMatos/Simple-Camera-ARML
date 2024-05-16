package com.simplemobiletools.camera.ar.arml.elements.gml

import com.simplemobiletools.camera.ar.arml.elements.ARML
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Namespace

abstract class GMLGeometries internal constructor(
	private val root: ARML,
	private val base: LowLevelGMLGeometries
) {

	internal constructor(root: ARML, other: GMLGeometries) : this(root, other.base)

	val id: String? = base.id

	override fun toString(): String {
		return "${this::class.simpleName}(gml:id=$id)"
	}

	abstract fun validate(): Pair<Boolean, String>
}




//REQ: http://www.opengis.net/spec/arml/2.0/req/model/GMLGeometries/interface
internal abstract class LowLevelGMLGeometries {

	@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
	@field:Attribute(name = "id", required = false)
	var id: String? = null
}
