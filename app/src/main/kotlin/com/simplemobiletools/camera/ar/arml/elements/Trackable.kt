package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

class Trackable internal constructor(
	private val root: ARML,
	private val base: LowLevelTrackable
) : ARAnchor(root, base) {

	internal constructor(root: ARML, other: Trackable) : this(root, other.base)

	val config: List<TrackableConfig> = base.config.map { TrackableConfig(root, it) }
	val size: Double? = base.size

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,assets=$assets,config=$config,size=$size)"
	}

	override fun validate(): Pair<Boolean, String> {
		val result = super.validate(); if (!result.first) return result
		config.forEach { val result1 = it.validate(); if (!result1.first) return result1 }
		return Pair(true, "Success")
	}
}

class TrackableConfig internal constructor(
	root: ARML,
	private val base: LowLevelTrackableConfig
) {

	internal constructor(root: ARML, other: TrackableConfig) : this(root, other.base)

	val tracker: String = base.tracker.href
	val src: String = base.src
	val order: Int? = base.order

	override fun toString(): String {
		return "Config(tracker=\"$tracker\",src=\"$src\",order=$order)"
	}

	fun validate(): Pair<Boolean, String> {
		return Pair(true, "Success")
	}
}




@Root(name = "Trackable", strict = true)
internal class LowLevelTrackable : LowLevelARAnchor() {

	@field:ElementList(name = "config", required = true, inline = true)
	var config: List<LowLevelTrackableConfig> = ArrayList()

	@field:Element(name = "size", required = false)
	var size: Double? = null
}

@Root(name = "config", strict = true)
internal class LowLevelTrackableConfig {

	@field:Element(name = "tracker", required = true)
	lateinit var tracker: TRACKER

	@Root(name = "tracker", strict = true)
	class TRACKER {
		@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
		@field:Attribute(name = "href", required = true)
		lateinit var href: String

		override fun toString(): String {
			return href
		}
	}

	@field:Element(name = "src", required = true)
	lateinit var src: String

	@field:Attribute(name = "order", required = false)
	var order: Int? = null
}
