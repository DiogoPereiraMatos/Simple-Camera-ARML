package com.simplemobiletools.camera.ar.armlparser.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root


@Root(name = "SelectedCondition", strict = true)
class SelectedCondition : Condition() {

	@field:Element(name = "selected", required = true)
	var selected: Boolean = true

	@field:Element(name = "listener", required = false)
	var listener: String? = null

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
