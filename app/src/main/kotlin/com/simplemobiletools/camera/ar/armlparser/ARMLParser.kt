package com.example.armlparser

import com.simplemobiletools.camera.ar.armlparser.elements.ARML
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister

class ARMLParser {

	private val serializer: Serializer = Persister()

	fun loads(xml: String): ARML? {
		return serializer.read(ARML::class.java, xml)
	}
}

//Try https://www.baeldung.com/jaxb
