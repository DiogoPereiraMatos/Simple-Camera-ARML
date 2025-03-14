package com.simplemobiletools.camera.ar.arml.elements

import java.lang.reflect.Field

abstract class PrintableElement {
	companion object {
		val ignoreFields = listOf("arElementType", "elementsById", "Companion", "ignoreFields")
	}

	//FIXME: Not very elegant
	override fun toString(): String {

		val allFields = ArrayList<Field>()
		var current: Class<*> = this::class.java
		var parent: Class<*>? = current.superclass
		while (parent != null) {
			allFields.addAll(current.declaredFields)
			current = parent
			parent = parent.superclass
		}

		val fieldStrings = ArrayList<String>()
		for (field in allFields.sortedBy { it.name }) {
			val wasAccessible = field.isAccessible
			field.isAccessible = true
			val name = field.name
			val value = field.get(this) ?: continue
			field.isAccessible = wasAccessible

			if (ignoreFields.contains(name)) {
				continue
			}

			val str = when (value) {
				is List<*> -> {
					if(value.isEmpty())
						continue
					"$name=${value.sortedBy { it.toString() }}"
				}
				is String -> "$name=\"${value.trimIndent().filterNot { it == '\n' }}\""
				else -> "$name=$value"
			}

			fieldStrings.add(str)
		}

		return "${this::class.java.simpleName}(${fieldStrings.joinToString(", ")})"
	}
}
