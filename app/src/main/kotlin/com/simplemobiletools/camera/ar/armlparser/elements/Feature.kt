package com.simplemobiletools.camera.ar.armlparser.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementListUnion
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

@Root(name = "Feature", strict = true)
class Feature : ARElement() {

	@field:Element(name = "name", required = false)
	var name: String? = null

	@field:Element(name = "description", required = false)
	var description: String? = null

	@field:Element(name = "enabled", required = false)
	var enabled: Boolean? = true

	// TODO: Any XML
	@field:ElementList(name = "metadata", required = false, inline = false)
	var metadata: List<String>? = null

	@field:Element(name = "anchors", required = false)
	var anchors: FeatureAnchors? = null

	class FeatureAnchors {

		@field:ElementList(name = "anchorRef", type = AnchorRef::class, inline = true, required = false)
		var anchorRefs: List<AnchorRef> = ArrayList()

		@Root(name = "anchorRef", strict = true)
		class AnchorRef {
			@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
			@field:Attribute(name = "href", required = true)
			lateinit var href: String

			override fun toString(): String {
				return href
			}

			fun validate(): Pair<Boolean, String> {
				return Pair(true, "Success")
			}
		}

		@field:ElementListUnion(
			// Anchors
			ElementList(name = "ScreenAnchor", type = ScreenAnchor::class, inline = true, required = false),
			// ARAnchors
			ElementList(name = "Geometry", type = Geometry::class, inline = true, required = false), ElementList(name = "RelativeTo", type = RelativeTo::class, inline = true, required = false),
			ElementList(name = "Trackable", type = Trackable::class, inline = true, required = false),
		)
		var anchors: List<Anchor> = ArrayList()

		override fun toString(): String {
			val result = ArrayList<Any>()
			result.addAll(anchorRefs)
			result.addAll(anchors)
			return result.toString()
		}

		fun validate(): Pair<Boolean, String> {
			anchorRefs.forEach { val result = it.validate(); if (!result.first) return result }
			anchors.forEach { val result1 = it.validate(); if (!result1.first) return result1 }
			return Pair(true, "Success")
		}
	}


	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",name=\"$name\",description=\"$description\",enabled=$enabled,metadata=$metadata,anchors=$anchors)"
	}

	override fun validate(): Pair<Boolean, String> {
		val result = anchors?.validate(); if (!result?.first!!) return result
		return Pair(true, "Success")
	}
}
