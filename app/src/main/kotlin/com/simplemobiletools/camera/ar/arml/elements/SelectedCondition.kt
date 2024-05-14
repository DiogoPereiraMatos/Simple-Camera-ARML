package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

class SelectedCondition internal constructor(
	private val root: ARML,
	private val base: LowLevelSelectedCondition
) : Condition(root, base) {

	internal constructor(root: ARML, other: SelectedCondition) : this(root, other.base)

	val selected: Boolean = base.selected
	val listener: String? = base.listener

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun validate(): Pair<Boolean, String> {
		if (listener?.lowercase() !in arrayOf("feature", "anchor")) return Pair(
			false,
			"Expected \"feature\" or \"anchor\" for \"listener\" element in ${this::class.simpleName}, got \"$listener\""
		)
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",listener=\"$listener\",selected=$selected)"
	}
}




@Root(name = "SelectedCondition", strict = true)
internal class LowLevelSelectedCondition : LowLevelCondition() {

	@field:Element(name = "selected", required = true)
	var selected: Boolean = true

	@field:Element(name = "listener", required = false)
	var listener: String? = null
}
