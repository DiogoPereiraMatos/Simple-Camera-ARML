package com.simplemobiletools.camera.ar.arml.elements

import dev.romainguy.kotlin.math.Float3
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementListUnion

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
					is LowLevelDistanceCondition -> result.add(DistanceCondition(root, it))
					else -> throw Exception("Unexpected VisualAsset Condition Type: $it")
				}
			}
			return result
		}

	val rotationVector: Float3 = Float3(
		orientation?.roll?.toFloat() ?: 0f,
		orientation?.heading?.toFloat() ?: 0f, //pan
		orientation?.tilt?.toFloat() ?: 0f,
	)

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

	@field:ElementListUnion(
		ElementList(name = "SelectedCondition", type = LowLevelSelectedCondition::class, inline = true, required = false),
		ElementList(name = "DistanceCondition", type = LowLevelDistanceCondition::class, inline = true, required = false),
	)
	var conditions: List<LowLevelCondition>? = null
}
