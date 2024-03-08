package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList


//REQ: http://www.opengis.net/spec/arml/2.0/req/model/VisualAsset/interface
abstract class VisualAsset : ARElement() {

	@field:Element(name = "enabled", required = false)
	var enabled: Boolean? = true

	@field:Element(name = "zOrder", required = false)
	var zOrder: Int? = null

	@field:Element(name = "Orientation", required = false)
	var orientation: Orientation? = null

	@field:Element(name = "ScalingMode", required = false)
	var scalingMode: ScalingMode? = null

	@field:ElementList(name = "Conditions", required = false, inline = false)
	var conditions: List<Condition>? = null

	override fun validate(): Pair<Boolean, String> {
		if (orientation != null) {
			val result = orientation!!.validate(); if (!result.first) return result
		}
		if (scalingMode != null) {
			val result1 = scalingMode!!.validate(); if (!result1.first) return result1
		}
		conditions?.forEach { val result2 = it.validate(); if (!result2.first) return result2 }
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,zOrder=$zOrder,orientation=$orientation,scalingMode=$scalingMode,conditions=$conditions)"
	}
}
