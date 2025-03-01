package com.codingblocks.clock.core

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codingblocks.clock.core.local.data.CustomFTAlert
import com.codingblocks.clock.core.local.data.CustomNFTAlert
import com.codingblocks.clock.core.local.data.FTPriceEntity
import com.codingblocks.clock.core.local.data.NFTStatsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class FetchPricesWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val dataRepo: DataRepo
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                Timber.tag("wims").i("doing work")
                // Fetch and store token prices
                dataRepo.getAndStorePricesForTokens()

                // Fetch and store policy prices
                dataRepo.getAndStorePricesForPolicies()

                Result.success()
            } catch (e: Exception) {
                Result.retry() // Retry if there's an error
            }
        }
    }
}

class FTAlertWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val dataRepo: DataRepo
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.tag("wims").i("FTAlertWorker doWork")
        return withContext(Dispatchers.IO) {
            try {
                // Fetch all enabled alerts
                val alerts = dataRepo.getAllEnabledFTAlerts()

                // Process each alert
                alerts.forEach { alert ->
                    Timber.tag("wims").i("check for alert: ${alert.ticker} ${alert.threshold} and priceOrVolume ${alert.priceOrVolume}")

                    // Fetch the latest two stats entries for the policy
                    val stats = dataRepo.getLatest2PricesForUnit(alert.feedPositionUnit)
                    Timber.tag("wims").i("prices.size == ${stats.size}")
                    if (stats.size == 2) {

                        val previousStat = stats[1]
                        val currentStat = stats[0]

                        Timber.tag("wims").i("previous price ${previousStat.price} treshold ${alert.threshold} current ${currentStat.price}")
                        // Check if the alert condition is met
                        val conditionMet = if (alert.priceOrVolume) {
                            checkIfNotAlreadyTriggeredByThesePrices(alert.lastTriggeredTimeStamp, previousStat.timestamp)
                                    && checkPriceCrossing(alert.crossingOver, alert.threshold, previousStat.price, currentStat.price)
                        } else {
                            // no volume yet on FTAlert
                            false
                        }

                        if (conditionMet) {
                            Timber.tag("wims").i("all conditions met ! lastTriggeredTimeStamp ${alert.lastTriggeredTimeStamp} << ${previousStat.timestamp} previous price.timestamp -> trigger for ${alert.ticker}!")
                            triggerAlerts(alert, currentStat)
                            if (alert.onlyOnce) {
                                dataRepo.deleteAlert(alert)
                            } else {
                                // update alert with lastAlerted Timestamp
                                dataRepo.setLastTriggered(alert)
                            }
                        }
                    }
                }

                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    private fun checkIfNotAlreadyTriggeredByThesePrices(lastTriggeredTimeStamp: Long?, previousPriceTimeStamp: Long): Boolean =
        if (lastTriggeredTimeStamp == null) true else lastTriggeredTimeStamp < previousPriceTimeStamp


    private fun checkPriceCrossing(
        crossingOver: Boolean,
        threshold: Double,
        previousPrice: Double,
        currentPrice: Double
    ): Boolean {
        return if (crossingOver) {
            previousPrice < threshold && currentPrice >= threshold
        } else {
            previousPrice > threshold && currentPrice <= threshold
        }
    }

    private suspend fun triggerAlerts(alert: CustomFTAlert, currentStat: FTPriceEntity) {
        if (alert.pushAlert) {
            Timber.tag("wims").i("push alert now for ${alert.ticker}")
            dataRepo.pushFTAlert(alert, currentStat.price)
        }
        if (alert.clockAlert) {
            dataRepo.addAlertFTToFeedToClockItems(alert, currentStat.price)
        }
        if (alert.mail) {
            // Send email alert (implement this in dataRepo if needed)
        }
    }
}

class NFTAlertWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val dataRepo: DataRepo
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.tag("wims").i("NFTAlertWorker doWork")
        return withContext(Dispatchers.IO) {
            try {
                // Fetch all enabled alerts
                val alerts = dataRepo.getAllEnabledNFTAlerts()

                // Process each alert
                alerts.forEach { alert ->
                    Timber.tag("wims").i("check for alert: ${alert.ticker} ${alert.threshold} and priceOrVolume ${alert.priceOrVolume}")

                    // Fetch the latest two stats entries for the policy
                    val stats = dataRepo.getLatest2StatsForPolicy(alert.feedPositionPolicy)
                    Timber.tag("wims").i("stats.size == ${stats.size}")
                    if (stats.size == 2) {

                        val previousStat = stats[1]
                        val currentStat = stats[0]

                        val conditionMet = if (alert.priceOrVolume) {
                            //checkIfNotAlreadyTriggeredByThesePrices(alert.lastTriggeredTimeStamp, previousStat.timestamp)
                                     checkPriceCrossing(alert, previousStat, currentStat)
                        } else {
                            checkVolumeChange(alert, previousStat, currentStat)
                        }
                        Timber.tag("wims").i("previous price ${previousStat.price} treshold ${alert.threshold} current ${currentStat.price}")

                        // If the condition is met, trigger alerts and handle onlyOnce
                        if (conditionMet) {
                            Timber.tag("wims").i("all conditions met ! lastTriggeredTimeStamp ${alert.lastTriggeredTimeStamp} << ${previousStat.timestamp} previous price.timestamp -> trigger for ${alert.ticker}!")
                            triggerAlerts(alert, currentStat)
                            if (alert.onlyOnce) {
                                dataRepo.deleteAlert(alert)
                            } else {
                                dataRepo.setLastTriggered(alert)
                            }
                        }
                    }
                }

                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    private fun checkIfNotAlreadyTriggeredByThesePrices(lastTriggeredTimeStamp: Long?, previousPriceTimeStamp: Long): Boolean =
        if (lastTriggeredTimeStamp == null) true else lastTriggeredTimeStamp < previousPriceTimeStamp


    private fun checkPriceCrossing(
        alert: CustomNFTAlert,
        previousStat: NFTStatsEntity,
        currentStat: NFTStatsEntity
    ): Boolean {
        val previousPrice = previousStat.price.toDouble()
        val currentPrice = currentStat.price.toDouble()

        return if (alert.crossingOver) {
            // Check if price crossed over the threshold
            previousPrice < alert.threshold && currentPrice >= alert.threshold
        } else {
            // Check if price crossed under the threshold
            previousPrice > alert.threshold && currentPrice <= alert.threshold
        }
    }

    private fun checkVolumeChange(
        alert: CustomNFTAlert,
        previousStat: NFTStatsEntity,
        currentStat: NFTStatsEntity
    ): Boolean {
        val previousVolume = previousStat.volume.toDouble()
        val currentVolume = currentStat.volume.toDouble()

        // Check if the volume change exceeds the threshold
        return Math.abs(currentVolume - previousVolume) >= alert.threshold
    }

    private suspend fun triggerAlerts(alert: CustomNFTAlert, currentStat: NFTStatsEntity) {
        if (alert.pushAlert) {
            Timber.tag("wims").i("push alert now for ${alert.ticker}")
            dataRepo.pushNFTAlert(alert, currentStat.price)
        }
        if (alert.clockAlert) {
            dataRepo.addAlertNFTToFeedToClockItems(alert, currentStat.price)
        }
        if (alert.mail) {
            // Send email alert (implement this in dataRepo if needed)
        }
    }
}