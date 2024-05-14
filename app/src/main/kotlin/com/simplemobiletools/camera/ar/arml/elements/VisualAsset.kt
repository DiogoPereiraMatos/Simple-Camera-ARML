package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList

abstract class VisualAsset internal constructor(
	private val root: ARML,
	private val base: LowLevelVisualAsset
) : ARElement(root, base) {

	internal constructor(root: ARML, other: VisualAsset) : this(root, other.base)

	val enabled: Boolean? = base.enabled
	val zOrder: Int? = base.zOrder
	val orientation: Orientation? = base.orientation?.let { Orientation(root, it) }
	val scalingMode: ScalingMode? = base.scalingMode?.let { ScalingMode(root, it) }
	val conditions : List<Condition>?
		get() {
			if (base.conditions == null) return null

			val result: ArrayList<Condition> = ArrayList()
			val lowLevelList = base.conditions!!
			lowLevelList.forEach {
				when(it) {
					is LowLevelSelectedCondition -> result.add(SelectedCondition(root, it))
					else -> throw Exception("Unexpected VisualAsset Condition Type: $it")
				}
			}
			return result
		}

	override fun validate(): Pair<Boolean, String> {
		if (orientation != null) {
			val result = orientation.validate(); if (!result.first) return result
		}
		if (scalingMode != null) {
			val result1 = scalingMode.validate(); if (!result1.first) return result1
		}
		conditions?.forEach { val result2 = it.validate(); if (!result2.first) return result2 }
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,zOrder=$zOrder,orientation=$orientation,scalingMode=$scalingMode,conditions=$conditions)"
	}
}




//REQ: http://www.opengis.net/spec/arml/2.0/req/model/VisualAsset/interface
internal abstract class LowLevelVisualAsset : LowLevelARElement() {

	@field:Element(name = "enabled", required = false)
	var enabled: Boolean? = true

	@field:Element(name = "zOrder", required = false)
	var zOrder: Int? = null

	@field:Element(name = "Orientation", required = false)
	var orientation: LowLevelOrientation? = null

	@field:Element(name = "ScalingMode", required = false)
	var scalingMode: LowLevelScalingMode? = null

	@field:ElementList(name = "Conditions", required = false, inline = false)
	var conditions: List<LowLevelCondition>? = null
}
