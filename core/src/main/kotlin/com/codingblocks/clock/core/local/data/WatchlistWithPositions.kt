package com.codingblocks.clock.core.local.data

import androidx.room.Embedded
import androidx.room.Relation
import com.codingblocks.clock.core.tokenAToPositionFT
import com.codingblocks.clock.core.tokenBToPositionFT
import java.time.ZonedDateTime

data class WatchlistWithPositions(
    @Embedded
    val watchListConfig: WatchListConfig,
    @Relation(
        parentColumn = "watchlistNumber",
        entityColumn = "watchList"
    )
    val positionsFT: List<PositionFTLocal>,
    @Relation(
        parentColumn = "watchlistNumber",
        entityColumn = "watchList"
    )
    val positionsNFT: List<PositionNFTLocal>,
    @Relation(
        parentColumn = "watchlistNumber",
        entityColumn = "watchList"
    )
    val positionsLP: List<PositionLPLocal>,
) {
    val positionsFTIncludingLP: List<PositionFTLocal>
        get() = getAllPositionsFTIncludingLP(
            watchListConfig.watchlistNumber,
            positionsLP,
            positionsFT,
        )

}

fun getAllPositionsFTIncludingLP(
    watchList: Int,
    cachedPositionsLP: List<PositionLPLocal>,
    cachedPositionsFT: List<PositionFTLocal>
): List<PositionFTLocal> {
    val ftUnits = cachedPositionsFT.map { it.unit }.toSet()

    // add adaValue and Balance for each PostionFT we already have
    val positionsFTIncludingLP = cachedPositionsFT.map { positionFT ->
        val (lpAdaValue, lpBalance) = calculateTotalAdaValueAndBalance(cachedPositionsLP, positionFT.unit)
        if (lpAdaValue != positionFT.adaValue) {
            positionFT.copy(
                adaValue = lpAdaValue + positionFT.adaValue,
                balance = lpBalance + positionFT.balance,
            )
        } else {
            positionFT
        }
    }
    // create FT positions for LP positions A and/or B that dont exist yet as a seperate FT position
    val positionsFtFromLP: MutableList<PositionFTLocal> = mutableListOf()
    cachedPositionsLP.mapNotNull { lp ->
        when {
            lp.tokenA !in ftUnits && lp.tokenB !in ftUnits -> {
                positionsFtFromLP.add(lp.tokenAToPositionFT(watchList))
                positionsFtFromLP.add(lp.tokenBToPositionFT(watchList))
            }
            lp.tokenA !in ftUnits -> positionsFtFromLP.add(lp.tokenAToPositionFT(watchList))
            lp.tokenB !in ftUnits -> positionsFtFromLP.add(lp.tokenBToPositionFT(watchList))
            else -> null
        }
    }

    // group multiple entries for same unt and merge into one
    val groupedPositions = positionsFtFromLP.groupBy { it.unit }
    val uniqueFTFromLP = groupedPositions.map { (unit, positionsForUnit) ->
        val mergedPosition = positionsForUnit.reduce { acc, position ->
            PositionFTLocal(
                unit = unit,
                fingerprint = position.fingerprint,
                adaValue = acc.adaValue + position.adaValue,
                price = acc.price,
                ticker = position.unit,
                balance = acc.balance + position.balance,
                change30D = acc.change30D,
                showInFeed = acc.showInFeed,
                watchList = acc.watchList,
                createdAt = acc.createdAt,
                lastUpdated = ZonedDateTime.now()
            )
        }
        mergedPosition
    }

    return (positionsFTIncludingLP + uniqueFTFromLP).sortedByDescending { it.adaValue }
}


fun calculateTotalAdaValueAndBalance(cachedPositionsLP: List<PositionLPLocal>, unit: String): Pair<Double, Double> {
    var totalAdaValue = 0.0
    var totalBalance = 0.0

    cachedPositionsLP.forEach { lp ->
        if (lp.tokenA == unit || lp.tokenB == unit) {
            totalAdaValue += lp.adaValue / 2
            totalBalance += when (unit) {
                lp.tokenA -> lp.tokenAAmount
                lp.tokenB -> lp.tokenBAmount
                else -> 0.0
            }
        }
    }

    return Pair(totalAdaValue, totalBalance)
}
