package com.simplemobiletools.camera.ar.arml

import com.simplemobiletools.camera.ar.arml.elements.*
import com.simplemobiletools.camera.ar.arml.elements.gml.*
import org.junit.Test

class ParsingTest {

	private val xsd_path : String = "src\\main\\kotlin\\com\\simplemobiletools\\camera\\ar\\arml\\arml.xsd"

	private val header : String = """xmlns="http://www.opengis.net/arml/2.0" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:xlink="http://www.w3.org/1999/xlink""""

	@Test
	fun simple_xml_arml_1() {
		val str = """
			<arml xmlns="http://www.opengis.net/arml/2.0" 
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> 
				<ARElements>
				</ARElements>
			</arml>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = parser.loads(str)
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_arml_2() {
		val str = """
			<arml $header>
				<ARElements>
					<Feature id=“myFeature”>
						<name>My first Feature</name>
						<anchors>
						</anchors>
					</Feature>
				</ARElements>
			
				<style type=“text/css”>
					<![CDATA[
					… CSS style definitions of any Visual Assets
					]]>
				</style> 
			
				<script type=“text/ecmascript”>  <!–might also be javascript and other derivatives –>
					<![CDATA[
					… ECMAScript goes here …     ]]>
				</script>
			</arml>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = parser.loads(str)
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_feature() {
		val str = """
			<Feature id="ferrisWheel" $header>
				<name>Ferris Wheel</name>
				<enabled>true</enabled>
				<metadata>
					<constructed>1896-1897</constructed>
					<height>64,75</height>
				</metadata>
				<anchors>
					<!-- either defined directly in the tag -->
					<Geometry>
						<assets>
							<assetRef xlink:href="#appleModel" />
							<assetRef xlink:href="#appleModel" />
						</assets>
						<gml:Point>
							<gml:pos>1 2</gml:pos>
						</gml:Point>
					</Geometry>
					<!-- or referenced -->
					<anchorRef xlink:href="#myAnchor" />
				</anchors>
			</Feature>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Feature(ARML(), parser.loads(str, LowLevelFeature::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_geometry() {
		val str = """				
			<Feature id=“myFeature” $header>
				<anchors>
					<Geometry>
						<enabled>true</enabled>
						<assets>
							<assetRef xlink:href="#appleModel" />
							<assetRef xlink:href="#appleModel" />
						</assets>
						<gml:Point gml:id=“point1”>
							<gml:pos>1 2</gml:pos>
						</gml:Point>
					</Geometry>
				</anchors>
			</Feature>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Feature(ARML(), parser.loads(str, LowLevelFeature::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_point_1() {
		val str = """
			<gml:Point gml:id="myPointWithAltitudeOfUser" $header>
				<gml:pos>
					47.48 13.14
				</gml:pos>
			</gml:Point>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Point(ARML(), parser.loads(str, LowLevelPoint::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_point_2() {
		val str = """
			<gml:Point gml:id="myPointWithExplicitAltitude" srsDimension="3" $header>
				<gml:pos>
					47.48 13.14 520
				</gml:pos>
			</gml:Point>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Point(ARML(), parser.loads(str, LowLevelPoint::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_linestring() {
		val str = """
			<gml:LineString gml:id="myLineString" $header>
				<gml:posList>
					47.48 13.14 48.49 14.15
				</gml:posList>
			</gml:LineString>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = LineString(ARML(), parser.loads(str, LowLevelLineString::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_polygon() {
		val str = """
			<gml:Polygon gml:id="myPolygon" $header>
				<gml:exterior>
					<gml:LinearRing>
						<gml:posList>
							47.48 13.14 48.49 14.15 48.49 14.13 47.48 13.14
						</gml:posList>
					</gml:LinearRing>
				</gml:exterior>
				<gml:interior>
					<gml:LinearRing>
						<gml:posList>
							48.00 14.00 48.01 14.01 48.01 13.99 48.00 14.00
						</gml:posList>
					</gml:LinearRing>
				</gml:interior>
				<gml:interior>
					<gml:LinearRing>
						<gml:posList>
							48.00 14.00 48.01 14.01 48.01 13.99 48.00 14.00
						</gml:posList>
					</gml:LinearRing>
				</gml:interior> 
			</gml:Polygon>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Polygon(ARML(), parser.loads(str, LowLevelPolygon::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_tracker_1() {
		val str = """
			<!-- a generic image Tracker -->
			<Tracker id="myGenericImageTracker" $header>
				<uri xlink:href="http://www.opengis.net/arml/tracker/genericImageTracker" />
			</Tracker>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Tracker(ARML(), parser.loads(str, LowLevelTracker::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_tracker_2() {
		val str = """
			<!-- a generic image Tracker operating on a set of image targets supplied via a zip file -->
			<Tracker id="myGenericImageTrackerWithZip" $header>
				<uri xlink:href="http://www.opengis.net/arml/tracker/genericImageTracker" />
				<src xlink:href="http://www.myserver.com/myTargets/myTargets.zip" />
			</Tracker>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Tracker(ARML(), parser.loads(str, LowLevelTracker::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_tracker_3() {
		val str = """
			<!-- a custom Tracker -->
			<Tracker id="myCustomTracker" $header>
				<uri xlink:href="http://www.myServer.com/myTracker" />
				<src xlink:href="http://www.myServer.com/myTrackables/binary.file" />
			</Tracker> 
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Tracker(ARML(), parser.loads(str, LowLevelTracker::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_trackable_1() {
		val str = """
			<!-- a png image tracked with the generic image tracker -->
			<Trackable id="myBirdTrackable" $header>
				<config>
					<tracker xlink:href="#myGenericImageTracker" />
					<src>http://www.myserver.com/myTrackables/bird.png</src>
				</config>
				<size>0.2</size> <!-- in real word dimensions, the bird image is 20 cm wide -->
				<assets>
					<assetRef xlink:href="#appleModel" />
					<assetRef xlink:href="#appleModel" />
				</assets>
			</Trackable>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Trackable(ARML(), parser.loads(str, LowLevelTrackable::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_trackable_2() {
		val str = """
			<!-- a jpg image tracked with the generic image tracker operating on a zip file-->
			<Trackable id="myBirdTrackableInZip" $header>
				<config>
					<tracker xlink:href="#myGenericImageTrackerWithZip" />
					<src>/images/bird.png</src>
				</config>
				<size>0.2</size>
				<assets>
					<assetRef xlink:href="#appleModel" />
					<assetRef xlink:href="#appleModel" />
				</assets>
			</Trackable>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Trackable(ARML(), parser.loads(str, LowLevelTrackable::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_trackable_3() {
		val str = """
			<!-- a jpg image tracked with the generic image tracker operating on a zip file-->
			<Trackable id="myCustomBirdTrackable" $header>
				<config>
					<tracker xlink:href="#myCustomTracker" />
					<src>bird</src> <!-- the custom tracker is supposed to understand the ID "bird" in the Tracker's binary container -->
				</config>
				<size>0.2</size>
				<assets>
					<assetRef xlink:href="#appleModel" />
					<assetRef xlink:href="#appleModel" />
				</assets>
			</Trackable>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Trackable(ARML(), parser.loads(str, LowLevelTrackable::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_trackable_4() {
		val str = """
			<!—a Trackable that can be tracked in two different ways, preferably with a custom implementation that takes a binary file, and if this configuration is not available, a generic imagetracker should be used-->
			<Trackable id="myTrackable" $header>
				<config order="1">
					<tracker xlink:href="#myCustomSuperSpeedyTracker" />
					<src>http://www.myserver.com/myTrackables/bird.dat</src>
				</config>
				<!—fallback -->
				<config order="2">
					<tracker xlink:href="#myGenericImageTracker" />
					<src>http://www.myserver.com/myTrackables/bird.png</src>
				</config>
				<size>0.2</size>
				<assets>
					<assetRef xlink:href="#appleModel" />
					<assetRef xlink:href="#appleModel" />
				</assets>
			</Trackable>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")
			.replace("—", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Trackable(ARML(), parser.loads(str, LowLevelTrackable::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_relativeto() {
		val str = """
			<!-- assuming a square Trackable with size 5 for this example-->
			<RelativeTo $header>
				<ref xlink:href="#myTrackable" />
				<gml:LineString gml:id="trackableOutline">
					<gml:posList dimension="3"> <!-- will describe the outline of the square marker (2.5 meters from origin to top, bottom, left and right edge -->
						2.5 2.5 0 2.5 -2.5 0 -2.5 -2.5 0 -2.5 2.5 0 2.5 2.5 0
					</gml:posList>
				</gml:LineString>
				<assets>
					<assetRef xlink:href="#appleModel" />
					<assetRef xlink:href="#appleModel" />
				</assets>
			</RelativeTo>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")
			.replace("—", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = RelativeTo(ARML(), parser.loads(str, LowLevelRelativeTo::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_screenanchor() {
		val str = """
			<Feature id="myPlacemark" $header>
				<anchors>
					<ScreenAnchor>
						<style>bottom:0; left:0; width: 100%;</style>
						<!-- area spans the entire screen width, and is located at the bottom of the screen; top is dynamic -->
						<assets>
							<Label>
								<src><![CDATA[<div><b>My Restaurant</b> is wonderful, come in and have a seat!</div>]]></src>
							</Label>
						</assets>
					</ScreenAnchor>
				</anchors>
			</Feature>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")
			.replace("—", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Feature(ARML(), parser.loads(str, LowLevelFeature::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_label_1() {
		val str = """
			<Label id="mySrcLabel" $header>
				<src>
					Here's my Label NOT in a div
				</src>
			</Label>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")
			.replace("—", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Label(ARML(), parser.loads(str, LowLevelLabel::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_label_2() {
		val str = """
			<Label id="myHrefLabel" $header>
				<href xlink:href="http://www.myserver.com/myLabel.html" />
			</Label>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")
			.replace("—", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Label(ARML(), parser.loads(str, LowLevelLabel::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_label_3() {
		val str = """
			<!-- The Label could be attached to multiple buildings conforming with the same metadata-layout -->
			<Label id="myBuildingLabel" $header>
				<src>
					${'$'}[name]
					Constructed: ${'$'}[/constructed]
					height: ${'$'}[/height]
				</src>
			</Label>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")
			.replace("—", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Label(ARML(), parser.loads(str, LowLevelLabel::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_fill_1() {
		val str = """
			<Fill id="myFill" $header>
				<style>color:#FF0000;</style>
			</Fill>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")
			.replace("—", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Fill(ARML(), parser.loads(str, LowLevelFill::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_fill_2() {
		val str = """
			<Fill id="myFill" $header>
				<class>redFill</class>
			</Fill>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")
			.replace("—", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Fill(ARML(), parser.loads(str, LowLevelFill::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_text() {
		val str = """
			<Text id="myText" $header>
				<style>font-color:#FF0000;</style>
				<src>This text will be displayed</src>
			</Text>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")
			.replace("—", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Text(ARML(), parser.loads(str, LowLevelText::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_image() {
		val str = """
			<Image id="myImage" $header>
				<href xlink:href="http://www.myserver.com/myImage.png" />
			</Image>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")
			.replace("—", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Image(ARML(), parser.loads(str, LowLevelImage::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_model() {
		val str = """
			<Model id="myModel" $header>
				<href xlink:href="http://domain.com/myColladaFile.zip" /> <!-- a URI to a zip file, containing the COLLADA dae file, textures and any other ressources required -->
				<type>infrastructure</type> <!-- one of normal|infrastructure -->
				<Orientation>
					<roll>0</roll>
					<tilt>0</tilt>
					<heading>0</heading> <!-- Model is oriented towards north -->
				</Orientation>
				<Scale>
					<x>1</x>
					<y>1</y>
					<z>1</z>
				</Scale>
				<zOrder>0</zOrder> <!-- int value controlling the rendering order (defaults to 0)-->  
			</Model>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")
			.replace("—", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = Model(ARML(), parser.loads(str, LowLevelModel::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_scalingmode_1() {
		val str = """
			<ScalingMode type="custom" $header>
				<minScalingDistance>50</minScalingDistance>
				<maxScalingDistance>5000</maxScalingDistance> 
				<scalingFactor>0.75</scalingFactor>
			</ScalingMode>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")
			.replace("—", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = ScalingMode(ARML(), parser.loads(str, LowLevelScalingMode::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_scalingmode_2() {
		val str = """
			<ScalingMode type="natural" $header /> <!-- this is the default behavior -->
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")
			.replace("—", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = ScalingMode(ARML(), parser.loads(str, LowLevelScalingMode::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_distancecondition_1() {
		val str = """
			<DistanceCondition $header>
				<min>200</min> <!-- only visible when distance is more than 200 meters -->
			</DistanceCondition>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")
			.replace("—", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = DistanceCondition(ARML(), parser.loads(str, LowLevelDistanceCondition::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_distancecondition_2() {
		val str = """
			<DistanceCondition $header>
				<max>500</max>
				<min>200</min> <!-- only visible when distance more than 200 meters, but less than 500 meters -->
			</DistanceCondition>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")
			.replace("—", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = DistanceCondition(ARML(), parser.loads(str, LowLevelDistanceCondition::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

	@Test
	fun simple_xml_selectedcondition() {
		val str = """
			<SelectedCondition $header>
				<listener>feature</listener>
				<selected>true</selected> <!-- only visible when the Feature the VisualAsset is attached to is selected -->
			</SelectedCondition>
		"""
			.trimIndent()
			.replace("“", "\"")
			.replace("”", "\"")
			.replace("–", "--")
			.replace("—", "--")

		val t_init = System.nanoTime() / 1_000_000
		val parser = ARMLParser()
		val t_start = System.nanoTime() / 1_000_000
		val obj = SelectedCondition(ARML(), parser.loads(str, LowLevelSelectedCondition::class.java))
		val t_load = System.nanoTime() / 1_000_000
		val result = obj.validate()
		val t_val = System.nanoTime() / 1_000_000

		if (result.first) println("OK") else {println("ERROR");println(result.second)}
		println("init: ${t_start-t_init} ms;load: ${t_load-t_start} ms; validate: ${t_val-t_load} ms")
		println(obj)

		assert(result.first)
	}

}
