package com.simplemobiletools.camera.ar.arml.elements

import dev.romainguy.kotlin.math.Float3
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementListUnion

abstract class VisualAsset : ARElement {
	var enabled: Boolean = true
	var zOrder: Int = 0
	val conditions: ArrayList<Condition> = ArrayList()
	var orientation: Orientation? = null
	var scalingMode: ScalingMode? = null

	//FIXME: Should be Roll-Tilt-Heading, no? As in roll-pitch-yaw?
	val rotationVector: Float3
		get() = Float3(
			orientation?.tilt?.toFloat() ?: 0f,
			orientation?.roll?.toFloat() ?: 0f,
			orientation?.heading?.toFloat() ?: 0f,
		)

	constructor() : super()

	constructor(other: VisualAsset) : super(other) {
		this.enabled = other.enabled
		this.zOrder = other.zOrder
		this.orientation = other.orientation
		this.scalingMode = other.scalingMode
		this.conditions.replaceAllWith(other.conditions)
	}

	override fun validate(): Pair<Boolean, String> {
		orientation?.let { orientation -> orientation.validate().let { if (!it.first) return it } }
		scalingMode?.let { scalingMode -> scalingMode.validate().let { if (!it.first) return it } }
		conditions.forEach { condition -> condition.validate().let { if (!it.first) return it } }
		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelVisualAsset) : super(base) {
		this.enabled = base.enabled ?: true
		this.zOrder = base.zOrder ?: 0
		this.orientation = base.orientation?.let { Orientation(root, it) }
		this.scalingMode = base.scalingMode?.let { ScalingMode(root, it) }

		val result: ArrayList<Condition> = ArrayList()
		if (base.conditions != null) {
			val lowLevelList = base.conditions!!
			lowLevelList.forEach {
				when (it) {
					is LowLevelSelectedCondition -> result.add(SelectedCondition(root, it))
					is LowLevelDistanceCondition -> result.add(DistanceCondition(root, it))
					else -> throw Exception("Unexpected VisualAsset Condition Type: $it")
				}
			}
		}
		this.conditions.replaceAllWith(result)
	}
}


internal abstract class LowLevelVisualAsset : LowLevelARElement() {

	@field:Element(name = "enabled", required = false)
	var enabled: Boolean? = null

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
