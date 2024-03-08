package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element


//REQ: http://www.opengis.net/spec/arml/2.0/req/model/VisualAsset2D/interface
abstract class VisualAsset2D : VisualAsset() {

	@field:Element(name = "width", required = false)
	var width: String? = null

	@field:Element(name = "height", required = false)
	var height: String? = null

	@field:Element(name = "orientationMode", required = false)
	var orientationMode: String? = null

	@field:Element(name = "backside", required = false)
	var backside: String? = null

	override fun validate(): Pair<Boolean, String> {
		if (orientationMode != null) if (orientationMode!!.lowercase() !in arrayOf(
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
