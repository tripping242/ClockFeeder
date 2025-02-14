package com.codingblocks.clock.base.ui.utils

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.Strictness
import com.google.gson.stream.JsonReader
import java.io.StringReader

fun <T> prettyPrintDataClass(data: T): String {
    val gson = GsonBuilder().setPrettyPrinting().create()
    return gson.toJson(data)
}

fun prettyPrintJson(
    json: String,
): String {
    return try {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val reader = JsonReader(StringReader(json))
        reader.strictness = Strictness.LENIENT
        val jsonElement = JsonParser.parseReader(reader)
        gson.toJson(jsonElement)
    } catch (e: JsonSyntaxException) {
        "Invalid JSON: ${e.message}"
    }
}