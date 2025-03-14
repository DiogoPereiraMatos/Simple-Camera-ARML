@file:Suppress("FunctionName", "LocalVariableName")

package com.simplemobiletools.camera.ar.arml

import com.simplemobiletools.camera.ar.arml.elements.*
import com.simplemobiletools.camera.ar.arml.elements.gml.*
import org.junit.Test
import kotlin.time.measureTimedValue

class ParsingTest {

	companion object {
		private const val HEADER : String = """xmlns="http://www.opengis.net/arml/2.0" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:xlink="http://www.w3.org/1999/xlink""""
		private val parser : ARMLParser = measureTimedValue { ARMLParser() }
			.also { println("Init: ${it.duration} ms") }
			.value
	}


	@Test
	fun misc_metadata() {
		val str = """
			<Feature $HEADER>
				<metadata>
					str1
					<test>elem1</test>
					str2
					<test>elem2</test>
					str3
					<ping>pong</ping>
				</metadata>
			</Feature>
		"""
			.trimIndent()

		val arml = ARML()

		val load = measureTimedValue { parser.loads(str, LowLevelFeature::class.java) }
		val obj = Feature(arml, load.value)
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)

		//FIXME: Will fail!
		assert(obj.metadata.toString() == """
			str1
			<test>elem1</test>
			str2
			<test>elem2</test>
			str3
			<ping>pong</ping>
		""".trimIndent())
	}


	@Test
	fun misc_user_id() {
		val str = """
			<Feature id="user" $HEADER>
			</Feature>
		"""
			.trimIndent()

		val arml = ARML()

		val load = measureTimedValue { parser.loads(str, LowLevelFeature::class.java) }
		val obj = Feature(arml, load.value)
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)

		assert(obj.id != "user") {
			AssertionError("ID is user")
		}
	}
	

	@Test
	fun simple_xml_arml_1() {
		val str = """
			<arml $HEADER> 
				<ARElements>
				</ARElements>
			</arml>
		"""
			.trimIndent()
		val expected = ARML()

		val load = measureTimedValue { parser.loads(str) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
		assert(obj == expected) {
			AssertionError("Expected $expected.")
		}
	}

	@Test
	fun simple_xml_arml_2() {
		val str = """
			<arml $HEADER>
				<ARElements>
					<Feature id="myFeature">
						<name>My first Feature</name>
						<anchors>
						</anchors>
					</Feature>
				</ARElements>
			
				<style type="text/css">
					<![CDATA[
					... CSS style definitions of any Visual Assets
					]]>
				</style> 
			
				<script type="text/ecmascript">  <!--might also be javascript and other derivatives -->
					<![CDATA[
					... ECMAScript goes here ...     ]]>
				</script>
			</arml>
		"""
			.trimIndent()
		val expected = ARML().apply {
			elements.add(Feature().apply {
				id = "myFeature"
				name = "My first Feature"
			})
			styles.add(Style("""
					... CSS style definitions of any Visual Assets
			""".trimIndent()).apply {
				type = "text/css"
			})
			scripts.add(Script("""
					... ECMAScript goes here ...
			""".trimIndent()).apply {
				type = "text/ecmascript"
			})
		}

		val load = measureTimedValue { parser.loads(str) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
		assert(obj == expected) {
			AssertionError("Expected $expected.")
		}
	}

	@Test
	fun simple_xml_feature() {
		val str = """
			<Feature id="ferrisWheel" $HEADER>
				<name>Ferris Wheel</name>
				<enabled>true</enabled>
				<metadata>
					<constructed>1896-1897</constructed>
					<height>64,75</height>
				</metadata>
				<anchors>
					<!-- either defined directly in the tag -->
					<Geometry id="myGeometry">
						<assets>
							<assetRef xlink:href="#appleModel" />
							<assetRef xlink:href="#appleModel" />
						</assets>
						<gml:Point id="applePosition">
							<gml:pos>1 2</gml:pos>
						</gml:Point>
					</Geometry>
					<!-- or referenced -->
					<anchorRef xlink:href="#myAnchor" />
				</anchors>
			</Feature>
		"""
			.trimIndent()
		val appleModel = Model(href = "").apply {
			id = "#appleModel"
		}
		val myAnchor = Trackable().apply {
			id = "#myAnchor"
			config.add(
				TrackableConfig("#placeholder_tracker", "placeholder_src")
			)
		}
		val expected = Feature().apply {
			id = "ferrisWheel"
			name = "Ferris Wheel"
			enabled = true
			metadata.replaceAllWith(listOf(
				"1896-1897",
				"64,75"
			))
			anchors.add(Geometry(
				Point(id = "applePosition", pos = listOf(1.0, 2.0))
			).apply {
				id = "myGeometry"
				assets.replaceAllWith(listOf(
					appleModel,
					appleModel
				))
			})
			anchors.add(myAnchor)
		}

		// Manually add elements
		val arml = ARML().apply {
			elements.add(appleModel)
			elements.add(myAnchor)
		}

		val load = measureTimedValue { Feature(arml, parser.loads(str, LowLevelFeature::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
		assert(obj == expected) {
			AssertionError("Expected $expected.")
		}
	}

	@Test
	fun simple_xml_geometry() {
		val str = """				
			<Feature id="myFeature" $HEADER>
				<anchors>
					<Geometry>
						<enabled>true</enabled>
						<assets>
							<assetRef xlink:href="#appleModel" />
							<assetRef xlink:href="#appleModel" />
						</assets>
						<gml:Point id="point1">
							<gml:pos>1 2</gml:pos>
						</gml:Point>
					</Geometry>
				</anchors>
			</Feature>
		"""
			.trimIndent()

		// Manually add elements
		val arml = ARML().apply {
			val appleModel = Model(href = "").apply {
				id = "#appleModel"
			}
			elements.add(appleModel)
		}

		val load = measureTimedValue { Feature(arml, parser.loads(str, LowLevelFeature::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_point_1() {
		val str = """
			<gml:Point id="myPointWithAltitudeOfUser" $HEADER>
				<gml:pos>
					47.48 13.14
				</gml:pos>
			</gml:Point>
		"""
			.trimIndent()

		val load = measureTimedValue { Point(ARML(), parser.loads(str, LowLevelPoint::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_point_2() {
		val str = """
			<gml:Point id="myPointWithExplicitAltitude" srsDimension="3" $HEADER>
				<gml:pos>
					47.48 13.14 520
				</gml:pos>
			</gml:Point>
		"""
			.trimIndent()

		val load = measureTimedValue { Point(ARML(), parser.loads(str, LowLevelPoint::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_linestring() {
		val str = """
			<gml:LineString id="myLineString" srsDimension="4" $HEADER>
				<gml:posList>
					47.48 13.14 48.49 14.15
				</gml:posList>
				<gml:Point id="applePosition">
					<gml:pos>1 2</gml:pos>
				</gml:Point>
				<gml:Point id="applePosition">
					<gml:pos>1 2</gml:pos>
				</gml:Point>
			</gml:LineString>
		"""
			.trimIndent()

		val load = measureTimedValue { LineString(ARML(), parser.loads(str, LowLevelLineString::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_polygon() {
		val str = """
			<gml:Polygon id="myPolygon" $HEADER>
				<gml:exterior>
					<gml:posList>
						47.48 13.14 48.49 14.15 48.49 14.13 47.48 13.14
					</gml:posList>
				</gml:exterior>
				<gml:LinearRing>
					<gml:posList>
						48.00 14.00 48.01 14.01 48.01 13.99 48.00 14.00
					</gml:posList>
				</gml:LinearRing>
				<gml:LinearRing>
					<gml:posList>
						48.00 14.00 48.01 14.01 48.01 13.99 48.00 14.00
					</gml:posList>
				</gml:LinearRing>
			</gml:Polygon>
		"""
			.trimIndent()

		val load = measureTimedValue { Polygon(ARML(), parser.loads(str, LowLevelPolygon::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_tracker_1() {
		val str = """
			<!-- a generic image Tracker -->
			<Tracker id="myGenericImageTracker" $HEADER>
				<uri xlink:href="http://www.opengis.net/arml/tracker/genericImageTracker" />
			</Tracker>
		"""
			.trimIndent()

		val load = measureTimedValue { Tracker(ARML(), parser.loads(str, LowLevelTracker::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_tracker_2() {
		val str = """
			<!-- a generic image Tracker operating on a set of image targets supplied via a zip file -->
			<Tracker id="myGenericImageTrackerWithZip" $HEADER>
				<uri xlink:href="http://www.opengis.net/arml/tracker/genericImageTracker" />
				<src xlink:href="http://www.myserver.com/myTargets/myTargets.zip" />
			</Tracker>
		"""
			.trimIndent()

		val load = measureTimedValue { Tracker(ARML(), parser.loads(str, LowLevelTracker::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_tracker_3() {
		val str = """
			<!-- a custom Tracker -->
			<Tracker id="myCustomTracker" $HEADER>
				<uri xlink:href="http://www.myServer.com/myTracker" />
				<src xlink:href="http://www.myServer.com/myTrackables/binary.file" />
			</Tracker> 
		"""
			.trimIndent()

		val load = measureTimedValue { Tracker(ARML(), parser.loads(str, LowLevelTracker::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_trackable_1() {
		val str = """
			<!-- a png image tracked with the generic image tracker -->
			<Trackable id="myBirdTrackable" $HEADER>
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

		// Manually add elements
		val arml = ARML().apply {
			val appleModel = Model(href = "").apply {
				id = "#appleModel"
			}
			elements.add(appleModel)
		}

		val load = measureTimedValue { Trackable(arml, parser.loads(str, LowLevelTrackable::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_trackable_2() {
		val str = """
			<!-- a jpg image tracked with the generic image tracker operating on a zip file-->
			<Trackable id="myBirdTrackableInZip" $HEADER>
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

		// Manually add elements
		val arml = ARML().apply {
			val appleModel = Model(href = "").apply {
				id = "#appleModel"
			}
			elements.add(appleModel)
		}

		val load = measureTimedValue { Trackable(arml, parser.loads(str, LowLevelTrackable::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_trackable_3() {
		val str = """
			<!-- a jpg image tracked with the generic image tracker operating on a zip file-->
			<Trackable id="myCustomBirdTrackable" $HEADER>
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

		// Manually add elements
		val arml = ARML().apply {
			val appleModel = Model(href = "").apply {
				id = "#appleModel"
			}
			elements.add(appleModel)
		}

		val load = measureTimedValue { Trackable(arml, parser.loads(str, LowLevelTrackable::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_trackable_4() {
		val str = """
			<!-- a Trackable that can be tracked in two different ways, preferably with a custom implementation that takes a binary file, and if this configuration is not available, a generic imagetracker should be used -->
			<Trackable id="myTrackable" $HEADER>
				<config order="1">
					<tracker xlink:href="#myCustomSuperSpeedyTracker" />
					<src>http://www.myserver.com/myTrackables/bird.dat</src>
				</config>
				<!-- fallback -->
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

		// Manually add elements
		val arml = ARML().apply {
			val appleModel = Model(href = "").apply {
				id = "#appleModel"
			}
			elements.add(appleModel)
		}

		val load = measureTimedValue { Trackable(arml, parser.loads(str, LowLevelTrackable::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_relativeto() {
		val str = """
			<!-- assuming a square Trackable with size 5 for this example-->
			<RelativeTo $HEADER>
				<ref xlink:href="#myTrackable" />
				<gml:LineString id="trackableOutline">
					<gml:posList dimension="3"> <!-- will describe the outline of the square marker (2.5 meters from origin to top, bottom, left and right edge -->
						2.5 2.5 0 2.5 -2.5 0 -2.5 -2.5 0 -2.5 2.5 0 2.5 2.5 0
					</gml:posList>
					<gml:Point id="applePosition">
						<gml:pos>1 2</gml:pos>
					</gml:Point>
					<gml:Point id="applePosition">
						<gml:pos>1 2</gml:pos>
					</gml:Point>
				</gml:LineString>
				<assets>
					<assetRef xlink:href="#appleModel" />
					<assetRef xlink:href="#appleModel" />
				</assets>
			</RelativeTo>
		"""
			.trimIndent()

		// Manually add elements
		val arml = ARML().apply {
			val appleModel = Model(href = "").apply {
				id = "#appleModel"
			}
			elements.add(appleModel)
		}

		val load = measureTimedValue { RelativeTo(arml, parser.loads(str, LowLevelRelativeTo::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_screenanchor() {
		val str = """
			<Feature id="myPlacemark" $HEADER>
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

		val load = measureTimedValue { Feature(ARML(), parser.loads(str, LowLevelFeature::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_label_1() {
		val str = """
			<Label id="mySrcLabel" $HEADER>
				<src>
					Here's my Label NOT in a div
				</src>
			</Label>
		"""
			.trimIndent()

		val load = measureTimedValue { Label(ARML(), parser.loads(str, LowLevelLabel::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_label_2() {
		val str = """
			<Label id="myHrefLabel" $HEADER>
				<href xlink:href="http://www.myserver.com/myLabel.html" />
			</Label>
		"""
			.trimIndent()

		val load = measureTimedValue { Label(ARML(), parser.loads(str, LowLevelLabel::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_label_3() {
		val str = """
			<!-- The Label could be attached to multiple buildings conforming with the same metadata-layout -->
			<Label id="myBuildingLabel" $HEADER>
				<src>
					${'$'}[name]
					Constructed: ${'$'}[/constructed]
					height: ${'$'}[/height]
				</src>
			</Label>
		"""
			.trimIndent()

		val load = measureTimedValue { Label(ARML(), parser.loads(str, LowLevelLabel::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_fill_1() {
		val str = """
			<Fill id="myFill" $HEADER>
				<style>color:#FF0000;</style>
			</Fill>
		"""
			.trimIndent()

		val load = measureTimedValue { Fill(ARML(), parser.loads(str, LowLevelFill::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_fill_2() {
		val str = """
			<Fill id="myFill" $HEADER>
				<class>redFill</class>
			</Fill>
		"""
			.trimIndent()

		val load = measureTimedValue { Fill(ARML(), parser.loads(str, LowLevelFill::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_text() {
		val str = """
			<Text id="myText" $HEADER>
				<style>font-color:#FF0000;</style>
				<src>This text will be displayed</src>
			</Text>
		"""
			.trimIndent()

		val load = measureTimedValue { Text(ARML(), parser.loads(str, LowLevelText::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_image() {
		val str = """
			<Image id="myImage" $HEADER>
				<href xlink:href="http://www.myserver.com/myImage.png" />
			</Image>
		"""
			.trimIndent()

		val load = measureTimedValue { Image(ARML(), parser.loads(str, LowLevelImage::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_model() {
		val str = """
			<Model id="myModel" $HEADER>
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

		val load = measureTimedValue { Model(ARML(), parser.loads(str, LowLevelModel::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_scalingmode_1() {
		val str = """
			<ScalingMode type="custom" $HEADER>
				<minScalingDistance>50</minScalingDistance>
				<maxScalingDistance>5000</maxScalingDistance> 
				<scalingFactor>0.75</scalingFactor>
			</ScalingMode>
		"""
			.trimIndent()

		val load = measureTimedValue { ScalingMode(ARML(), parser.loads(str, LowLevelScalingMode::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_scalingmode_2() {
		val str = """
			<ScalingMode type="natural" $HEADER /> <!-- this is the default behavior -->
		"""
			.trimIndent()

		val load = measureTimedValue { ScalingMode(ARML(), parser.loads(str, LowLevelScalingMode::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_distancecondition_1() {
		val str = """
			<DistanceCondition $HEADER>
				<min>200</min> <!-- only visible when distance is more than 200 meters -->
			</DistanceCondition>
		"""
			.trimIndent()

		val load = measureTimedValue { DistanceCondition(ARML(), parser.loads(str, LowLevelDistanceCondition::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_distancecondition_2() {
		val str = """
			<DistanceCondition $HEADER>
				<max>500</max>
				<min>200</min> <!-- only visible when distance more than 200 meters, but less than 500 meters -->
			</DistanceCondition>
		"""
			.trimIndent()

		val load = measureTimedValue { DistanceCondition(ARML(), parser.loads(str, LowLevelDistanceCondition::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}

	@Test
	fun simple_xml_selectedcondition() {
		val str = """
			<SelectedCondition $HEADER>
				<listener>feature</listener>
				<selected>true</selected> <!-- only visible when the Feature the VisualAsset is attached to is selected -->
			</SelectedCondition>
		"""
			.trimIndent()

		val load = measureTimedValue { SelectedCondition(ARML(), parser.loads(str, LowLevelSelectedCondition::class.java)) }
		val obj = load.value
		val validate = measureTimedValue { obj.validate() }
		val result = validate.value
		println("load: ${load.duration.inWholeMilliseconds} ms; validate: ${validate.duration.inWholeMilliseconds} ms")

		assert(result.first) {
			AssertionError(result.second)
		}
		println("OK")
		println(obj)
	}
}
