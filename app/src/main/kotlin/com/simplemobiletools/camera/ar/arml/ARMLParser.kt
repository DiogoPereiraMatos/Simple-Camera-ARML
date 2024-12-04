package com.simplemobiletools.camera.ar.arml

import com.simplemobiletools.camera.ar.arml.elements.ARML
import com.simplemobiletools.camera.ar.arml.elements.LowLevelARML
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.convert.AnnotationStrategy
import org.simpleframework.xml.core.Persister
import org.simpleframework.xml.strategy.TreeStrategy

class ARMLParser {

	companion object {
		const val EMPTY = """
			<arml xmlns="http://www.opengis.net/arml/2.0" 
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> 
				<ARElements>
				</ARElements>
			</arml>
		"""
	}

	//private val serializer: Serializer = Persister(AnnotationStrategy(CycleStrategy()))
	private val serializer: Serializer = Persister(AnnotationStrategy(TreeStrategy()))

	@Throws(Exception::class)
	fun loads(xml: String): ARML {
		return ARML(serializer.read(LowLevelARML::class.java, xml))
	}

	@Throws(Exception::class)
	internal fun <T> loads(xml: String, root: Class<T>): T {
		return serializer.read(root, xml)
	}
}

