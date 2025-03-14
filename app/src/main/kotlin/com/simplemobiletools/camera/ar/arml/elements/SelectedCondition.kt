package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

enum class Listener {
	FEATURE,
	ANCHOR;

	override fun toString(): String {
		return this.name.lowercase()
	}
}

class SelectedCondition : Condition {
	override val arElementType = ARElementType.SELECTEDCONDITION

	var selected: Boolean = true
	var listener: Listener = Listener.ANCHOR

	constructor() : super()

	constructor(other: SelectedCondition) : super(other) {
		this.selected = other.selected
		this.listener = other.listener
	}

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun validate(): Pair<Boolean, String> = SUCCESS


	internal constructor(root: ARML, base: LowLevelSelectedCondition) : super(base) {
		this.selected = base.selected

		if (base.listener != null) {
			try {
				this.listener = Listener.valueOf(base.listener!!.uppercase())
			} catch (e: IllegalArgumentException) {
				val possibleValues = Listener.entries.map { it.toString() }
				throw Exception("Expected one of $possibleValues for \"listener\" element in ${this::class.simpleName}, got \"${base.listener}\"")
			}
		}
	}
}


@Root(name = "SelectedCondition", strict = true)
internal class LowLevelSelectedCondition : LowLevelCondition() {

	@field:Element(name = "selected", required = true)
	var selected: Boolean = true  // Booleans can't be lateinit

	@field:Element(name = "listener", required = false)
	var listener: String? = null
}
