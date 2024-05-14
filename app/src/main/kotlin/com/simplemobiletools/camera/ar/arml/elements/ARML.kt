package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementListUnion
import org.simpleframework.xml.Root


class ARML() {

	private var base: LowLevelARML = LowLevelARML()
	internal constructor(base: LowLevelARML) : this() {
		this.base = base
	}

	internal constructor(other: ARML) : this(other.base)

	val elements: List<ARElement>
		get() {
			val result = ArrayList<ARElement>()
			val lowLevelList = base.elements.elements
			lowLevelList.forEach {
				when(it) {
					is LowLevelFeature -> result.add(Feature(this, it))
					is LowLevelTracker -> result.add(Tracker(this, it))
					is LowLevelScreenAnchor -> result.add(ScreenAnchor(this, it))
					is LowLevelGeometry -> result.add(Geometry(this, it))
					is LowLevelRelativeTo -> result.add(RelativeTo(this, it))
					is LowLevelTrackable -> result.add(Trackable(this, it))
					is LowLevelSelectedCondition -> result.add(SelectedCondition(this, it))
					is LowLevelDistanceCondition -> result.add(DistanceCondition(this, it))
					is LowLevelModel -> result.add(Model(this, it))
					is LowLevelFill -> result.add(Fill(this, it))
					is LowLevelImage -> result.add(Image(this, it))
					is LowLevelLabel -> result.add(Label(this, it))
					is LowLevelText -> result.add(Text(this, it))
					else -> throw Exception("Unexpected ARML Element Type: $it")
				}
			}
			return result
		}

	val scripts: List<Script> = base.scripts.map { Script(this, it) }
	val styles: List<Style> = base.styles.map { Style(this, it) }

	val elementsById: HashMap<String, ARElement>
		get() {
			val result: HashMap<String, ARElement> = HashMap()
			elements.forEach {
				if (it.id != null)
					result[it.id] = it
				result.putAll(it.elementsById)
			}
			return result
		}

	fun validate(): Pair<Boolean, String> {
		elements.forEach { val result = it.validate(); if (!result.first) return result }
		scripts.forEach { val result1 = it.validate(); if (!result1.first) return result1 }
		styles.forEach { val result2 = it.validate(); if (!result2.first) return result2 }
		return Pair(true, "Success")
	}

	override fun toString(): String {
		return "${this::class.simpleName}(ARElements=$elements,scripts=$scripts,styles=$styles)"
	}
}




//REQ: http://www.opengis.net/spec/arml/2.0/req/model/general/root_element
@Root(name = "arml", strict = true)
internal class LowLevelARML {

	//REQ: http://www.opengis.net/spec/arml/2.0/req/model/ARElement/container
	@field:Element(name = "ARElements", required = true)
	lateinit var elements: ARElements

	@Root(name = "ARElements")
	internal class ARElements {

		//REQ: http://www.opengis.net/spec/arml/2.0/req/model/ARElement/container
		@field:ElementListUnion(
			ElementList(name = "Feature", type = LowLevelFeature::class, inline = true, required = false),
			ElementList(name = "Tracker", type = LowLevelTracker::class, inline = true, required = false),

			// Anchors
			ElementList(name = "ScreenAnchor", type = LowLevelScreenAnchor::class, inline = true, required = false),
			// ARAnchors
			ElementList(name = "Geometry", type = LowLevelGeometry::class, inline = true, required = false),
			ElementList(name = "RelativeTo", type = LowLevelRelativeTo::class, inline = true, required = false),
			ElementList(name = "Trackable", type = LowLevelTrackable::class, inline = true, required = false),

			// Conditions
			ElementList(name = "SelectedCondition", type = LowLevelSelectedCondition::class, inline = true, required = false),
			ElementList(name = "DistanceCondition", type = LowLevelDistanceCondition::class, inline = true, required = false),

			// VisualAssets
			ElementList(name = "Model", type = LowLevelModel::class, inline = true, required = false),
			// VisualAssets2D
			ElementList(name = "Fill", type = LowLevelFill::class, inline = true, required = false),
			ElementList(name = "Image", type = LowLevelImage::class, inline = true, required = false),
			ElementList(name = "Label", type = LowLevelLabel::class, inline = true, required = false),
			ElementList(name = "Text", type = LowLevelText::class, inline = true, required = false),
		)
		var elements: List<LowLevelARElement> = ArrayList()
	}

	@field:ElementList(name = "script", required = false, inline = true)
	var scripts: List<LowLevelScript> = ArrayList()

	@field:ElementList(name = "style", required = false, inline = true)
	var styles: List<LowLevelStyle> = ArrayList()
}
