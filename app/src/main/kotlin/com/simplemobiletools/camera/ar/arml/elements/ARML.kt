package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementListUnion
import org.simpleframework.xml.Root

internal fun<E> ArrayList<E>.replaceAllWith(other: List<E>) {
	this.clear()
	this.addAll(other)
}

enum class ARElementType(val className: String) {
	FEATURE           (Feature::class.simpleName!!),
	TRACKABLE         (Trackable::class.simpleName!!),
	RELATIVETO        (RelativeTo::class.simpleName!!),
	SCREENANCHOR      (ScreenAnchor::class.simpleName!!),
	GEOMETRY          (Geometry::class.simpleName!!),
	DISTANCECONDITION (DistanceCondition::class.simpleName!!),
	FILL              (Fill::class.simpleName!!),
	IMAGE             (Image::class.simpleName!!),
	LABEL             (Label::class.simpleName!!),
	MODEL             (Model::class.simpleName!!),
	SELECTEDCONDITION (SelectedCondition::class.simpleName!!),
	TEXT              (Text::class.simpleName!!),
	TRACKER           (Tracker::class.simpleName!!),
}

val SUCCESS = Pair(true, "Success")

class ARML: PrintableElement {
	val elements: ArrayList<ARElement> = ArrayList()
	val scripts: ArrayList<Script> = ArrayList()
	val styles: ArrayList<Style> = ArrayList()

	constructor() : super()

	constructor(other: ARML) : this() {
		this.elements.replaceAllWith(other.elements)
		this.scripts.replaceAllWith(other.scripts)
		this.styles.replaceAllWith(other.styles)
	}

	val elementsById: HashMap<String, ARElement>
		get() {
			val result = HashMap<String, ARElement>()
			elements.forEach {
				if (it.id != null)
					result[it.id!!] = it
				result.putAll(it.elementsById)
			}
			return result
		}

	fun validate(): Pair<Boolean, String> {
		elements.forEach { element -> element.validate().let { if (!it.first) return it } }
		scripts.forEach { script -> script.validate().let { if (!it.first) return it } }
		styles.forEach { style -> style.validate().let { if (!it.first) return it } }
		return SUCCESS
	}

	override fun equals(other: Any?): Boolean {
		// This is scuffed
		return toString() == other.toString()
	}

	override fun hashCode(): Int {
		// This is also scuffed
		return toString().hashCode()
	}


	internal constructor(base: LowLevelARML) : this() {
		this.elements.clear()
		base.elements.elements?.forEach {
			val element: ARElement = when (it) {
				is LowLevelFeature -> Feature(this, it)
				is LowLevelTracker -> Tracker(this, it)
				is LowLevelScreenAnchor -> ScreenAnchor(this, it)
				is LowLevelGeometry -> Geometry(this, it)
				is LowLevelRelativeTo -> RelativeTo(this, it)
				is LowLevelTrackable -> Trackable(this, it)
				is LowLevelSelectedCondition -> SelectedCondition(this, it)
				is LowLevelDistanceCondition -> DistanceCondition(this, it)
				is LowLevelModel -> Model(this, it)
				is LowLevelFill -> Fill(this, it)
				is LowLevelImage -> Image(this, it)
				is LowLevelLabel -> Label(this, it)
				is LowLevelText -> Text(this, it)
				else -> throw Exception("Unexpected ARML Element Type: $it")
			}
			this.elements.add(element)
		}

		base.scripts?.let { this.scripts.replaceAllWith(it.map { Script(this, it) }) }
		base.styles?.let { this.styles.replaceAllWith(it.map { Style(this, it) }) }
	}
}


@Root(name = "arml", strict = true)
internal class LowLevelARML {

	@field:Element(name = "ARElements", required = true)
	lateinit var elements: ARElements

	@Root(name = "ARElements")
	internal class ARElements {

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
		var elements: List<LowLevelARElement>? = null
	}

	@field:ElementList(name = "script", required = false, inline = true)
	var scripts: List<LowLevelScript>? = null

	@field:ElementList(name = "style", required = false, inline = true)
	var styles: List<LowLevelStyle>? = null
}
