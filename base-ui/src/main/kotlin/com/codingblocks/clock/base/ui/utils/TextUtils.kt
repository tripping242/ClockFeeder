package com.codingblocks.clock.base.ui.utils

import android.annotation.SuppressLint
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

@SuppressLint("DefaultLocale")
fun Double.formatMax8decimals(): String {
    return when {
        this >= 100 -> String.format("%.0f", this).removeSuffix(".0")
        this >= 1 -> String.format("%.4f", this).removeSuffix("0").removeSuffix("0").removeSuffix("0").removeSuffix("0").removeSuffix(".")
        this >= 0.01 -> String.format("%.6f", this).removeSuffix("0").removeSuffix("0").removeSuffix(".")
        else -> String.format("%.8f", this).removeSuffix("0").removeSuffix("0").removeSuffix(".")
    }
}

fun Double.formatToNoDecimals(): String = this.toLong().toString()