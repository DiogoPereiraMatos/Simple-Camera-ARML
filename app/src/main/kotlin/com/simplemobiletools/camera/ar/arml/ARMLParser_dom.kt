package com.example.armlparser

import org.w3c.dom.Document
import org.xml.sax.SAXException
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.validation.SchemaFactory


class ARMLParser_dom(xsd_path: String) {

	private val docBuilder = DocumentBuilderFactory
		.newInstance()
		.newDocumentBuilder()

	private val validator = SchemaFactory
		.newInstance("http://www.w3.org/2001/XMLSchema")
		.newSchema(File(xsd_path))
		.newValidator()

	fun loads(xml: String): Document {
		val doc = docBuilder.parse(xml.byteInputStream())
		doc.documentElement.normalize()
		return doc
	}

	fun validate(document: Document): Boolean {
		return try {
			validator.validate(DOMSource(document))
			true
		} catch (e : SAXException) {
			println(e)
			false
		}
	}
}

