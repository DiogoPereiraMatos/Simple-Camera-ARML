package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import kotlin.random.Random


abstract class ARElement internal constructor(
	private val root: ARML,
	private val base: LowLevelARElement
) {

	internal constructor(root: ARML, other: ARElement) : this(root, other.base)

	val id: String = base.id ?: Random(System.currentTimeMillis()).nextBytes(16).toString()

	abstract val elementsById: HashMap<String, ARElement>

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\")"
	}

	abstract fun validate(): Pair<Boolean, String>

	override fun equals(other: Any?): Boolean {
		// This is scuffed
		return toString() == other.toString()
	}

	override fun hashCode(): Int {
		// This is also scuffed
		return toString().hashCode()
	}
}




//REQ: http://www.opengis.net/spec/arml/2.0/req/model/ARElement/interface
internal abstract class LowLevelARElement {

	//REQ: http://www.opengis.net/spec/arml/2.0/req/model/ARElement/id
	@field:Attribute(name = "id", required = false)
	var id: String? = null
}
