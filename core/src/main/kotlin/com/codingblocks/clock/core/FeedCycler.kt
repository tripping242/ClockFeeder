package com.codingblocks.clock.core

import android.os.Handler
import android.os.Looper
import com.codingblocks.clock.core.local.data.FeedToClockItem
import timber.log.Timber

class FeedCycler(
    private val dataRepo: DataRepo
) {
    private val handler = Handler(Looper.getMainLooper())
    private var isPaused = false

    fun startCycling(cycleTime: Long) {
        Timber.tag("wims").i("start cycling")
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!isPaused) {
                    val items = dataRepo.getAllFeedToClockItems()
                    Timber.tag("wims").i("dataRepo.getAllFeedToClockItems() returns ${items.size} items")

                    if (items.isNotEmpty()) {
                        val item = items[0]
                        // Process the item (e.g., display it)
                        processItem(item)

                        if (item.deleteWhenDone) {
                            dataRepo.deleteFeedToClockItem(item)
                        }

                        // Move the item to the end of the list
                        dataRepo.deleteFeedToClockItem(item)
                        dataRepo.insertFeedToClockItem(item)
                    }
                }
                handler.postDelayed(this, cycleTime)
            }
        }, cycleTime)
    }

    fun pauseCycling() {
        isPaused = true
        handler.postDelayed({ isPaused = false }, 60000) // Pause for 1 minute
    }

    private fun processItem(item: FeedToClockItem) {
        Timber.tag("wims").i("could be doing something like display on clock")
        // Implement your logic to process the item (e.g., display it)
    }
}