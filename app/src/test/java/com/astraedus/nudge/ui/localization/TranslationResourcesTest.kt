package com.astraedus.nudge.ui.localization

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

/** A missing entry/argument must fail CI rather than silently fall back to English. */
class TranslationResourcesTest {
    private val resources = listOf(File("src/main/res"), File("app/src/main/res"))
        .first { it.isDirectory }
    private val locales = listOf("values-b+zh+Hans", "values-ja", "values-fr")
    private fun entries(folder: String): Map<String, Element> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(File(resources, "$folder/strings.xml"))
        val nodes = doc.documentElement.childNodes
        return (0 until nodes.length).mapNotNull { nodes.item(it) as? Element }
            .associateBy { it.getAttribute("name") }
    }
    private fun parameters(value: String): List<String> =
        Regex("%(?:[0-9]+\\$)?[sdf]").findAll(value).map { it.value }.sorted().toList()

    @Test fun allLanguagesHaveEveryTranslatableEntryAndEveryArgument() {
        val source = entries("values").filterValues { it.getAttribute("translatable") != "false" }
        locales.forEach { locale ->
            val translated = entries(locale)
            assertEquals("Resource names in $locale", source.keys, translated.keys)
            source.forEach { (key, original) ->
                val actual = translated.getValue(key)
                assertEquals("Resource type $locale/$key", original.tagName, actual.tagName)
                when (original.tagName) {
                    "string" -> {
                        assertTrue("Empty $locale/$key", actual.textContent.isNotBlank())
                        assertEquals("Format args $locale/$key", parameters(original.textContent), parameters(actual.textContent))
                    }
                    "string-array", "plurals" -> {
                        val en = original.getElementsByTagName("item")
                        val target = actual.getElementsByTagName("item")
                        assertEquals("Item count $locale/$key", en.length, target.length)
                        (0 until en.length).forEach { index ->
                            assertEquals("Item args $locale/$key/$index", parameters(en.item(index).textContent), parameters(target.item(index).textContent))
                        }
                    }
                    else -> fail("Unchecked resource type ${original.tagName}")
                }
            }
        }
    }

    @Test fun appLocaleListIncludesExactlyTheAvailableLanguages() {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(File(resources, "xml/locales_config.xml"))
        val nodes = doc.getElementsByTagName("locale")
        val languages = (0 until nodes.length).map {
            (nodes.item(it) as Element).getAttribute("android:name")
        }
        assertEquals(listOf("en", "zh-Hans", "ja", "fr"), languages)
        val build = File(resources, "../../../build.gradle.kts").canonicalFile.readText()
        assertTrue("Offline switches require bundled languages in AABs", build.contains("enableSplit = false"))
    }
}
