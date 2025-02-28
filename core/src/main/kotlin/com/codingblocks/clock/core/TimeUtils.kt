package com.codingblocks.clock.core

import android.annotation.SuppressLint

fun priceNotTooOld(createdTimeStamp: Long, tooOldSeconds: Int) {}

@SuppressLint("DefaultLocale")
fun formatNFTPrice(price: Int): String {
    return when {
        price < 100_000 -> price.toString() // Keep as is if ≤ 5 digits
        price < 1_000_000 -> String.format("%.1fk", price / 1_000.0).take(5) // Shorten to 5 chars max
        else -> String.format("%.1fm", price / 1_000_000.0).take(5) // Shorten to 5 chars max
    }
}

fun splitAndFormatName(name: String): List<Triple<Int, String, String>> {
    val words = name.split(" ") // Split by spaces
    val formattedTexts = mutableListOf<Triple<Int, String, String>>()

    val overTexts = mutableListOf<String>()
    val underTexts = mutableListOf<String>()

    var position = 0

    for (word in words) {
        var index = 0
        while (index < word.length) {
            val chunk = word.substring(index, minOf(index + 4, word.length))

            if (position < 6) {
                overTexts.add(chunk) // Fill over first (positions 0-5)
            } else {
                underTexts.add(chunk) // If over is full, start under
            }

            index += 4
            position++
        }
    }

    // Pair overTexts with underTexts (0-5)
    for (i in 0..5) {
        val over = overTexts.getOrNull(i) ?: ""
        val under = underTexts.getOrNull(i) ?: ""
        formattedTexts.add(Triple(i, over, under))
    }

    return formattedTexts
}
