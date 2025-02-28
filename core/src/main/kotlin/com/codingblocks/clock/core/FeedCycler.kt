package com.codingblocks.clock.core

import android.annotation.SuppressLint
import com.codingblocks.clock.core.local.data.FeedToClockItem
import com.codingblocks.clock.core.local.data.FeedType
import com.codingblocks.clock.core.manager.ClockApiError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class FeedCycler(
    private val dataRepo: DataRepo
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isPaused = false

    fun startCycling(defaultCycleTime: Long) {
        Timber.tag("wims").i("start cycling")
        scope.launch {
            var currentDelay = defaultCycleTime
            while (true) {
                if (!isPaused) {
                    val items = dataRepo.getAllFeedToClockItems()
                    Timber.tag("wims").i("dataRepo.getAllFeedToClockItems() returns ${items.size} items")

                    if (items.isNotEmpty()) {
                        val item = items[0]
                        // Process the item , on success continue, else retry after returned timeout seconds.
                        val (success, retryDelay) = processItem(item)

                        if (success) {
                            if (item.deleteWhenDone) {
                                dataRepo.deleteFeedToClockItem(item)
                            } else {
                                // Move the item to the end of the list
                                dataRepo.moveFeedToClockItemToTheEnd(item)
                            }
                            currentDelay = defaultCycleTime
                        } else {
                            currentDelay = retryDelay
                        }
                    }
                }
                delay(currentDelay)
            }
        }
    }

    fun pauseCycling() {
        isPaused = true
        scope.launch {
            delay(60000)
            isPaused = false
        }
    }

    fun stopCycling() {
        scope.cancel() // Cancel the coroutine scope when no longer needed
    }

    private suspend fun processItem(item: FeedToClockItem) : Pair<Boolean, Long> {
        Timber.tag("wims").i("could be doing something like display on clock")
        // Implement your logic to process the item (e.g., display it)
        return when (item.feedType) {
            FeedType.FeedFT -> {
                val encodedPrice = "/${formatPrice(item.price.toString())}"
                val pair = formatPair(item.name)
                dataRepo.sendFTPriceFeed(encodedPrice, pair, item.colorMode)
                    .onFailure { throwable ->
                        if (throwable is ClockApiError.TooManyRequests) {
                            Timber.tag("wims").i("we will wait for ${throwable.waitTime}")
                            Pair(false, throwable.waitTime.toLong())
                        } else {
                            // skip?
                            Pair(true, 0)
                        }
                    }
                Pair(true, 0)
            }
            FeedType.FeedNFT -> {
                item.price?.let {
                    dataRepo.sendNFTPriceFeed(item.name, price = item.price.toInt(), item.colorMode)
                }
                Pair(true, 0)
            }
            FeedType.AlertFT -> {
                Pair(true, 0)
            }
            FeedType.AlertNFT -> {
                Pair(true, 0)
            }

            FeedType.Custom -> {
                Pair(true, 0)
            }
        }
    }

    private fun formatPair(name: String): String {
        return name.take(4).uppercase() + "/ADA"
    }

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
}

@SuppressLint("DefaultLocale")
fun formatPrice(input: String): String {
    // Detect and clean up input format
    val cleaned = input.replace(" ", "")
    val normalized = if (cleaned.count { it == ',' } > 1 || (cleaned.contains('.') && cleaned.contains(','))) {
        cleaned.replace(",", "") // Assume comma is a thousand separator, remove it
    } else {
        cleaned.replace(',', '.') // Assume comma is decimal separator
    }

    // Parse to a Double
    val number = normalized.toDoubleOrNull() ?: return "ERROR"

    val price = when {
        number >= 100000 -> number.toLong().toString() // No decimal for 6-digit numbers
        number >= 10000 ->
            if (number % 1.0 == 0.0) number.toLong().toString() else String.format(Locale.US, "%.1f", number) // Round to whole number
        number % 1.0 == 0.0 -> number.toLong().toString() // Remove .0 for whole numbers
        number >= 1 -> number.toString().take(7) // Keep max possible decimals within 7 positions
        else -> DecimalFormat("0.######", DecimalFormatSymbols(Locale.US)).format(number).take(7) // Small decimals
    }
    Timber.tag("wims").i("price $price")

    val returnValue = replaceEmptySpace(price)
    Timber.tag("wims").i("returned value :$returnValue")
    return returnValue
}

fun replaceEmptySpace(price: String) : String =
    price.padStart(priceLeadingEmptySpace(price), '/')

fun priceLeadingEmptySpace(price: String) =
    if (price.contains(".")) 7 else 6