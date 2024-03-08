package com.simplemobiletools.camera.ar.arml.elements

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementListUnion
import org.simpleframework.xml.Root

//REQ: http://www.opengis.net/spec/arml/2.0/req/model/general/root_element
@Root(name = "arml", strict = true)
class ARML {

	//REQ: http://www.opengis.net/spec/arml/2.0/req/model/ARElement/container
	@field:Element(name = "ARElements", required = true)
	lateinit var elements: ARElements

	@Root(name = "ARElements")
	class ARElements {

		//REQ: http://www.opengis.net/spec/arml/2.0/req/model/ARElement/container
		@field:ElementListUnion(
			ElementList(name = "Feature", type = Feature::class, inline = true, required = false),
			ElementList(name = "Tracker", type = Tracker::class, inline = true, required = false),

			// Anchors
			ElementList(name = "ScreenAnchor", type = ScreenAnchor::class, inline = true, required = false),
			// ARAnchors
			ElementList(name = "Geometry", type = Geometry::class, inline = true, required = false),
			ElementList(name = "RelativeTo", type = RelativeTo::class, inline = true, required = false),
			ElementList(name = "Trackable", type = Trackable::class, inline = true, required = false),

			// Conditions
			ElementList(name = "SelectedCondition", type = SelectedCondition::class, inline = true, required = false),
			ElementList(name = "DistanceCondition", type = DistanceCondition::class, inline = true, required = false),

			// VisualAssets
			ElementList(name = "Model", type = Model::class, inline = true, required = false),
			// VisualAssets2D
			ElementList(name = "Fill", type = Fill::class, inline = true, required = false),
			ElementList(name = "Image", type = Image::class, inline = true, required = false),
			ElementList(name = "Label", type = Label::class, inline = true, required = false),
			ElementList(name = "Text", type = Text::class, inline = true, required = false),
		)
		var elements: List<ARElement> = ArrayList()

		override fun toString(): String {
			return elements.toString()
		}

		fun validate(): Pair<Boolean, String> {
			elements.forEach { val result = it.validate(); if (!result.first) return result }
			return Pair(true, "Success")
		}
	}

	@field:ElementList(name = "script", required = false, inline = true)
	var scripts: List<Script> = ArrayList()

	@field:ElementList(name = "style", required = false, inline = true)
	var styles: List<Style> = ArrayList()

	override fun toString(): String {
		return "${this::class.simpleName}(ARElements=$elements,scripts=$scripts,styles=$styles)"
	}

	fun validate(): Pair<Boolean, String> {
		val result = elements.validate(); if (!result.first) return result
		scripts.forEach { val result1 = it.validate(); if (!result1.first) return result1 }
		styles.forEach { val result2 = it.validate(); if (!result2.first) return result2 }
		return Pair(true, "Success")
	}
}
