package com.astraedus.nudge.ui.localization

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

/** Existing source-contract checks inspect the English copy through its real resource reference. */
fun resolveEnglishResourceCalls(source: String): String {
    val file = listOf(File("src/main/res/values/strings.xml"), File("app/src/main/res/values/strings.xml"))
        .first { it.exists() }
    val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getElementsByTagName("string")
    val values = (0 until nodes.length).associate { index ->
        val node = nodes.item(index) as Element
        node.getAttribute("name") to node.textContent.removeSurrounding("\"")
            .replace("\\'", "'").replace("\\\"", "\"")
    }
    return Regex("""strings\.getString\(R\.string\.(\w+)\)""").replace(source) { match ->
        val value = values[match.groupValues[1]] ?: error("Missing resource ${match.groupValues[1]}")
        "\"${value.replace("\"", "\\\"")}\""
    }
}
