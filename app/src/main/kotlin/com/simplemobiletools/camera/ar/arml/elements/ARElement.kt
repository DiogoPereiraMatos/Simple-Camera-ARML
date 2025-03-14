package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import kotlin.random.Random


abstract class ARElement: PrintableElement {
	abstract val arElementType: ARElementType

	var id: String? = null
	val uid: String = Random(System.nanoTime()).nextBytes(16).toString()

	constructor() : super()

	constructor(other: ARElement) : this() {
		this.id = other.id
	}

	internal abstract val elementsById: HashMap<String, ARElement>

	open fun validate(): Pair<Boolean, String> = SUCCESS

	fun toShortString(): String {
		return "${this::class.simpleName}(id=${this.id}, uid=${this.uid})"
	}

	override fun equals(other: Any?): Boolean {
		// This is scuffed
		return toString() == other.toString()
	}

	override fun hashCode(): Int {
		// This is also scuffed
		return toString().hashCode()
	}


	internal constructor(base: LowLevelARElement) : this() {
		this.id = when (val id = base.id) {
			"user", null -> this.id
			else -> id
		}
	}
}


internal abstract class LowLevelARElement {

	@field:Attribute(name = "id", required = false)
	var id: String? = null
}
