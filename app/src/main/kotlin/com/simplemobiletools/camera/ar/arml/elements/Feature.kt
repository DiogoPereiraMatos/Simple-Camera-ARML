package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.*

class Feature : ARElement {
	override val arElementType: ARElementType = ARElementType.FEATURE

	var name: String? = null
	var description: String? = null
	var enabled: Boolean = true
	val metadata: ArrayList<String> = ArrayList() //TODO: Any XML
	val anchors: ArrayList<Anchor> = ArrayList()

	constructor() : super()

	override val elementsById: HashMap<String, ARElement>
		get() {
			val result = HashMap<String, ARElement>()
			anchors.forEach {
				if (it.id != null)
					result[it.id!!] = it
				result.putAll(it.elementsById)
			}
			return result
		}

	override fun validate(): Pair<Boolean, String> {
		anchors.forEach { anchor -> anchor.validate().let { if (!it.first) return it } }
		return SUCCESS
	}


	internal constructor(root: ARML, base: LowLevelFeature) : super(base) {
		this.name = base.name
		this.description = base.description
		this.enabled = base.enabled ?: true
		base.metadata?.let { this.metadata.replaceAllWith(it) }

		val result = ArrayList<Anchor>()
		base.anchors?.anchors?.forEach {
			when (it) {
				is LowLevelScreenAnchor -> result.add(ScreenAnchor(root, it))
				is LowLevelGeometry -> result.add(Geometry(root, it))
				is LowLevelRelativeTo -> result.add(RelativeTo(root, it))
				is LowLevelTrackable -> result.add(Trackable(root, it))
				else -> throw Exception("Unexpected Feature Anchor Type: $it")
			}
		}
		base.anchors?.anchorRefs?.forEach {
			val referred = root.elementsById[it.href] ?: throw Exception("Reference to unknown element: ${it.href}")
			if (referred !is Anchor) throw Exception("${it.href} Expected reference to anchor but got: $referred")
			result.add(referred)
		}
		this.anchors.replaceAllWith(result)
	}
}


@Root(name = "Feature", strict = true)
internal class LowLevelFeature : LowLevelARElement() {

	@field:Element(name = "name", required = false)
	var name: String? = null

	@field:Element(name = "description", required = false)
	var description: String? = null

	@field:Element(name = "enabled", required = false)
	var enabled: Boolean? = null

	// TODO: Any XML
	@field:ElementList(name = "metadata", required = false, inline = false)
	var metadata: List<String>? = null

	@field:Element(name = "anchors", required = false)
	var anchors: FeatureAnchors? = null

	internal class FeatureAnchors {

		@field:ElementList(name = "anchorRef", type = AnchorRef::class, inline = true, required = false)
		var anchorRefs: List<AnchorRef>? = null

		@Root(name = "anchorRef", strict = true)
		internal class AnchorRef {
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
		var anchors: List<LowLevelAnchor>? = null
	}
}
