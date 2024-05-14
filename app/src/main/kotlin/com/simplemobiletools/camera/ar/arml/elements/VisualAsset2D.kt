package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element

abstract class VisualAsset2D internal constructor(
	private val root: ARML,
	private val base: LowLevelVisualAsset2D
) : VisualAsset(root, base) {

	internal constructor(root: ARML, other: VisualAsset2D) : this(root, other.base)

	val width: String? = base.width
	val height: String? = base.height
	val orientationMode: String? = base.orientationMode
	val backside: String? = base.backside

	override fun validate(): Pair<Boolean, String> {
		if (orientationMode != null) if (orientationMode.lowercase() !in arrayOf(
				"user",
				"absolute",
				"auto"
			)
		) return Pair(
			false,
			"Expected \"user\", \"absolute\", or \"auto\" for \"orientationMode\" element in ${this::class.simpleName}, got \"$orientationMode\""
		)
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",enabled=$enabled,zOrder=$zOrder,orientation=$orientation,scalingMode=$scalingMode,conditions=$conditions,width=\"$width\",height=\"$height\",orientationMode=\"$orientationMode\",backside=\"$backside\")"
	}
}




//REQ: http://www.opengis.net/spec/arml/2.0/req/model/VisualAsset2D/interface
internal abstract class LowLevelVisualAsset2D : LowLevelVisualAsset() {

	@field:Element(name = "width", required = false)
	var width: String? = null

	@field:Element(name = "height", required = false)
	var height: String? = null

	@field:Element(name = "orientationMode", required = false)
	var orientationMode: String? = null

	@field:Element(name = "backside", required = false)
	var backside: String? = null
}
