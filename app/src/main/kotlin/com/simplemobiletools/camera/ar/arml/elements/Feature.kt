package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementListUnion
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root


class Feature internal constructor(
	private val root: ARML,
	private val base: LowLevelFeature
) : ARElement(root, base) {

	internal constructor(root: ARML, other: Feature) : this(root, other.base)

	val name: String? = base.name
	val description: String? = base.description
	val enabled: Boolean? = base.enabled
	val metadata: List<String>? = base.metadata

	val anchors: ArrayList<Any>
		get() {
			val result = ArrayList<Any>()
			if (base.anchors != null) {
				result.addAll(base.anchors!!.anchorRefs)
				base.anchors!!.anchors.forEach {
					when(it) {
						is LowLevelScreenAnchor -> result.add(ScreenAnchor(root, it))
						is LowLevelGeometry -> result.add(Geometry(root, it))
						is LowLevelRelativeTo -> result.add(RelativeTo(root, it))
						is LowLevelTrackable -> result.add(Trackable(root, it))
						else -> throw Exception("Unexpected Feature Anchor Type: $it")
					}
				}
			}
			return result
		}

	override val elementsById: HashMap<String, ARElement>
		get() {
			val result: HashMap<String, ARElement> = HashMap()
			anchors.forEach {
				when(it) {
					is ScreenAnchor -> { it.id?.let { id -> result[id] = it}; result.putAll(it.elementsById) }
					is Geometry -> { it.id?.let { id -> result[id] = it}; result.putAll(it.elementsById) }
					is RelativeTo -> { it.id?.let { id -> result[id] = it}; result.putAll(it.elementsById) }
					is Trackable -> { it.id?.let { id -> result[id] = it}; result.putAll(it.elementsById) }
					else -> throw Exception("Unexpected Feature Anchor Type: $it")
				}
			}
			return result
		}

	override fun toString(): String {
		return "${this::class.simpleName}(id=\"$id\",name=\"$name\",description=\"$description\",enabled=$enabled,metadata=$metadata,anchors=$anchors)"
	}

	override fun validate(): Pair<Boolean, String> {
		anchors.filterIsInstance(Anchor::class.java).forEach { val result1 = it.validate(); if (!result1.first) return result1 }
		return Pair(true, "Success")
	}
}




@Root(name = "Feature", strict = true)
internal class LowLevelFeature : LowLevelARElement() {

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

	internal class FeatureAnchors {

		@field:ElementList(name = "anchorRef", type = AnchorRef::class, inline = true, required = false)
		var anchorRefs: List<AnchorRef> = ArrayList()

		@Root(name = "anchorRef", strict = true)
		class AnchorRef {
			@Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
			@field:Attribute(name = "href", required = true)
			lateinit var href: String
		}

		@field:ElementListUnion(
			// Anchors
			ElementList(name = "ScreenAnchor", type = LowLevelScreenAnchor::class, inline = true, required = false),
			// ARAnchors
			ElementList(name = "Geometry", type = LowLevelGeometry::class, inline = true, required = false),
			ElementList(name = "RelativeTo", type = LowLevelRelativeTo::class, inline = true, required = false),
			ElementList(name = "Trackable", type = LowLevelTrackable::class, inline = true, required = false),
		)
		var anchors: List<LowLevelAnchor> = ArrayList()
	}
}
