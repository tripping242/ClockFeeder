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
import kotlin.math.roundToInt

class FeedCycler(
    private val dataRepo: DataRepo
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isPaused = false
    private var isCycling = false
    private val priceCheckEvery: Long = 5 * 60 * 1000
    private var timePassedSinceLastPriceCheck: Long = 0

    fun isCycling(): Boolean = isCycling

    fun startCycling(defaultCycleTime: Long) {
        isCycling = true
        Timber.i("start cycling feed to clock")
        scope.launch {
            var currentDelay = defaultCycleTime
            while (true) {
                if (!isPaused) {
                    val items = dataRepo.getAllFeedToClockItems()
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
                            // for the next item, see if we need to update with price and colorMode on trend
                            dataRepo.loadAndUpdateNextFeedToClockItem()
                        } else {
                            currentDelay = retryDelay
                        }
                    }
                    // while we cycle the feed to the clock, be more active with retrieving prices
                    if (timePassedSinceLastPriceCheck > priceCheckEvery) {
                        launch {
                            dataRepo.getAndStorePricesForTokens()
                            dataRepo.checkFTAlertsAfterPriceUpdates()
                            timePassedSinceLastPriceCheck = 0
                        }
                    } else {
                        timePassedSinceLastPriceCheck += currentDelay
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
        isCycling = false
        scope.cancel()
    }

    private suspend fun processItem(item: FeedToClockItem) : Pair<Boolean, Long> {
        return when (item.feedType) {
            FeedType.FeedFT -> {
                val encodedPrice = "/${formatPrice(item.price.toString())}"
                val pair = formatPair(item.name)
                dataRepo.sendFTPriceFeed(encodedPrice, pair, item.colorMode)
                    .onFailure { throwable ->
                        if (throwable is ClockApiError.TooManyRequests) {
                            Pair(false, throwable.waitTime.toLong())
                        } else {
                            // skip
                            Pair(true, 0)
                        }
                    }
                Pair(true, 0)
            }
            FeedType.FeedNFT -> {
                item.price?.let {
                    dataRepo.sendNFTPriceFeed(item.name, price = item.price.roundToInt(), item.colorMode)
                }
                Pair(true, 0)
            }
            FeedType.AlertFT -> {
                item.price?.let {
                    dataRepo.sendFTPriceAlert(
                        item.name,
                        formatPrice(item.price.toString()),
                        formatPrice(item.unit),
                        item.colorMode
                    )
                }
                Pair(true, 0)
            }
            FeedType.AlertNFT -> {
                item.price?.let {
                    dataRepo.sendNFTPriceAlert(
                        item.name,
                        formatNFTPrice(item.price.roundToInt()),
                        formatNFTPrice(item.unit.toDouble().roundToInt()),
                        item.colorMode
                    )
                }
                Pair(true, 0)
            }

            FeedType.Custom -> {
                Pair(true, 0)
            }
        }
    }

    private fun formatPair(name: String): String {
        return name.take(5).uppercase() + "/ADA"
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
}

@SuppressLint("DefaultLocale")
fun formatPrice(input: String): String {
    // Detect and clean up input format
    val cleaned = input.replace(" ", "")
    val normalized = if (cleaned.count { it == ',' } > 1 || (cleaned.contains('.') && cleaned.contains(','))) {
        cleaned.replace(",", "")
    } else {
        cleaned.replace(',', '.')
    }

    // Parse to a Double
    val number = normalized.toDoubleOrNull() ?: return "ERROR"

    val price = when {
        number >= 100000 -> number.toLong().toString()
        number >= 10000 ->
            if (number % 1.0 == 0.0) number.toLong().toString() else String.format(Locale.US, "%.1f", number)
        number % 1.0 == 0.0 -> number.toLong().toString()
        number >= 1 -> number.toString().take(7)
        number >= 0.0001 -> DecimalFormat("0.######", DecimalFormatSymbols(Locale.US)).format(number).take(7)
        else -> DecimalFormat("0.00E0", DecimalFormatSymbols(Locale.US)).format(number).take(8)
    }
    return price
}

@SuppressLint("DefaultLocale")
fun formatNFTPriceFromDouble(price: Double): String {
    return when {
        price < 100_000 -> price.toString()
        price < 1_000_000 -> String.format("%.1fk", price / 1_000.0).take(5)
        else -> String.format("%.1fm", price / 1_000_000.0).take(5)
    }
}

@SuppressLint("DefaultLocale")
fun formatPrice5Digits(price: Double): String {
    return when {
        price >= 100_000 -> String.format("%.1fk", price / 1_000).take(5)
        price >= 1_000 -> String.format("%.0f", price).take(5)
        price >= 1.0 -> String.format("%.2f", price).take(5)
        price >= 0.001 -> String.format("%.4f", price).trimEnd('0').take(5)
        else -> DecimalFormat("0.0E0").format(price).take(5)
    }
}

fun replaceEmptySpace(price: String) : String =
    price.padStart(priceLeadingEmptySpace(price), '/')

fun priceLeadingEmptySpace(price: String) =
    if (price.contains(".")) 7 else 6