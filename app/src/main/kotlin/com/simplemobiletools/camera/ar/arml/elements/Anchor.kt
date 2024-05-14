package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element

abstract class Anchor internal constructor(
	private val root: ARML,
	private val base: LowLevelAnchor
) : ARElement(root, base) {

	internal constructor(root: ARML, other: Anchor) : this(root, other.base)

	val enabled: Boolean? = base.enabled

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled)"
	}

	override fun validate(): Pair<Boolean, String> {
		return Pair(true, "Success")
	}
}




//REQ: http://www.opengis.net/spec/arml/2.0/req/model/Anchor/interface
internal abstract class LowLevelAnchor : LowLevelARElement() {

	@field:Element(name = "enabled", required = false)
	var enabled: Boolean? = true
}
