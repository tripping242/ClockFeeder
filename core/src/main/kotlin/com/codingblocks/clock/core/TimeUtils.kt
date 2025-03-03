package com.codingblocks.clock.core

import android.annotation.SuppressLint

fun priceNotTooOld(createdTimeStamp: Long, tooOldSeconds: Int) {}

@SuppressLint("DefaultLocale")
fun formatNFTPrice(price: Int): String {
    return when {
        price < 100_000 -> price.toString()
        price < 1_000_000 -> String.format("%.1fk", price / 1_000.0).take(5)
        else -> String.format("%.1fm", price / 1_000_000.0).take(5)
    }
}

fun splitAndFormatName(name: String): List<Triple<Int, String, String>> {
    val words = name.split(" ")
    val formattedTexts = mutableListOf<Triple<Int, String, String>>()

    val overTexts = mutableListOf<String>()
    val underTexts = mutableListOf<String>()

    var position = 0

    for (word in words) {
        var index = 0
        while (index < word.length) {
            val chunk = word.substring(index, minOf(index + 4, word.length))

            if (position < 6) {
                overTexts.add(chunk)
            } else {
                underTexts.add(chunk)
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

fun splitAndFormatPrices(crossedPrice: String, reachedPrice: String): List<Triple<Int, String, String>> {
    val mid = reachedPrice.length / 2

    val reachedFirst = reachedPrice.substring(0, mid)
    val reachedSecond = reachedPrice.substring(mid)

    val crossedFirst = crossedPrice.substring(0, mid)
    val crossedSecond = crossedPrice.substring(mid)

    return listOf(
        Triple(4, reachedFirst, crossedFirst),
        Triple(5, reachedSecond, crossedSecond)
    )
}

fun splitAndFormatNameAlertFirst3Positions(name: String): List<Triple<Int, String, String>> {
    val words = name.split(" ")
    val formattedTexts = mutableListOf<Triple<Int, String, String>>()

    val overTexts = mutableListOf<String>()
    val underTexts = mutableListOf<String>()

    var position = 0

    for (word in words) {
        var index = 0
        while (index < word.length) {
            val chunk = word.substring(index, minOf(index + 4, word.length))

            if (position < 3) {
                overTexts.add(chunk)
            } else {
                underTexts.add(chunk)
            }

            index += 4
            position++
        }
    }

    // Pair overTexts with underTexts (0-5)
    for (i in 0..2) {
        val over = overTexts.getOrNull(i) ?: ""
        var under = underTexts.getOrNull(i) ?: ""
        if (i == 2 && underTexts.getOrNull(3) != null) under = under.take(2) + "..."
        formattedTexts.add(Triple(i, over, under))
    }

    return formattedTexts
}


