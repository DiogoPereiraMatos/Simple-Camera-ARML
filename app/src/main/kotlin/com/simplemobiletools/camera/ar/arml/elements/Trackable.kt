package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.*

class Trackable : ARAnchor {
	override val arElementType: ARElementType = ARElementType.TRACKABLE

	var size: Double? = null
	val config: ArrayList<TrackableConfig> = ArrayList()

	val sortedConfig: ArrayList<TrackableConfig>
		get() = ArrayList(config.sortedBy { it.order })

	constructor() : super()

	constructor(other: Trackable) : super(other) {
		this.size = other.size
		this.config.replaceAllWith(other.config)
	}

	override fun validate(): Pair<Boolean, String> {
		super.validate().let { if (!it.first) return it }
		if (config.size < 1)
			return Pair(
				false,
				"Trackable must have at least one config: $this"
			)
		config.forEach { config -> config.validate().let { if (!it.first) return it } }
		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelTrackable) : super(root, base) {
		this.size = base.size
		this.config.replaceAllWith(base.config.map { TrackableConfig(root, it) })
	}
}


@Root(name = "Trackable", strict = true)
internal class LowLevelTrackable : LowLevelARAnchor() {

	@field:ElementList(name = "config", required = true, inline = true)
	lateinit var config: List<LowLevelTrackableConfig>

	@field:Element(name = "size", required = false)
	var size: Double? = null
}





class TrackableConfig: PrintableElement {
	var tracker: String
	var src: String
	var order: Int = Int.MAX_VALUE

	constructor(tracker: String, src: String) : super() {
		this.tracker = tracker
		this.src = src
	}

	constructor(other: TrackableConfig) : this(other.tracker, other.src) {
		this.order = other.order
	}

	fun validate(): Pair<Boolean, String> = SUCCESS


	internal constructor(root: ARML, base: LowLevelTrackableConfig) : this(base.tracker.href, base.src) {
		this.order = base.order ?: 0
	}
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





class Tracker : ARElement {
	override val arElementType = ARElementType.TRACKER

	var uri: String
	var src: String? = null

	constructor(uri: String) : super() {
		this.uri = uri
	}

	constructor(other: Tracker) : super(other) {
		this.uri = other.uri
		this.src = other.src
	}

	override val elementsById: HashMap<String, ARElement> = HashMap()

	override fun validate(): Pair<Boolean, String> = SUCCESS


	internal constructor(root: ARML, base: LowLevelTracker) : super(base) {
		this.uri = base.uri.href
		this.src = base.src?.href
	}
}


@Root(name = "Tracker", strict = true)
internal class LowLevelTracker : LowLevelARElement() {

	@field:Element(name = "uri", required = true)
	lateinit var uri: URI

	@Root(name = "uri", strict = true)
	class URI {
		@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
		@field:Attribute(name = "href", required = true)
		lateinit var href: String
	}

	@field:Element(name = "src", required = false)
	var src: SRC? = null

	@Root(name = "src", strict = true)
	class SRC {
		@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
		@field:Attribute(name = "href", required = true)
		lateinit var href: String
	}
}
