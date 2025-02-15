package com.codingblocks.clock.core.local.dao

import androidx.room.Dao
import com.codingblocks.clock.core.local.data.PositionFTLocal
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.codingblocks.clock.core.local.data.PositionLPLocal
import com.codingblocks.clock.core.local.data.PositionNFTLocal
import java.time.ZonedDateTime

@Dao
interface PositionsDao {
    // FT Positions, ticker is the name, fingerprint is unique

    @Query("SELECT * FROM positionFT ORDER by adaValue DESC")
    fun getAllFTPositions() : List<PositionFTLocal>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFT(positionFTLocal: PositionFTLocal): Long

    @Transaction
    fun insertOrUpdateFT(positionFTLocal: PositionFTLocal) {
        val id = insertFT(positionFTLocal)
        if (id == -1L) { // Insert failed, so update the existing entry
            updateExistingFT(
                positionFTLocal.unit,
                positionFTLocal.adaValue,
                positionFTLocal.price,
                positionFTLocal.balance,
                positionFTLocal.change30D,
                positionFTLocal.lastUpdated,
            )
        }
    }

    @Transaction
    fun insertOrUpdateFTList(positions: List<PositionFTLocal>) {
        positions.forEach {
            insertOrUpdateFT(it) }
    }

    @Query("UPDATE positionFT SET adaValue = :adaValue, price = :price, balance = :balance, change30D = :change30D, lastUpdated = :lastUpdated WHERE unit = :unit")
    fun updateExistingFT(unit: String, adaValue: Double, price: Double, balance: Double, change30D: Double, lastUpdated: ZonedDateTime)

    // NFT Positions, name is the name, policy is unique

    @Query("SELECT * FROM positionNFT ORDER by adaValue DESC")
    fun getAllNFTPositions() : List<PositionNFTLocal>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNFT(positionNFTLocal: PositionNFTLocal): Long

    @Update
    fun updateNFT(positionNFTLocal: PositionNFTLocal)

    @Transaction
    fun insertOrUpdateNFT(positionNFTLocal: PositionNFTLocal) {
        val id = insertNFT(positionNFTLocal)
        if (id == -1L) { // Insert failed, so update the existing entry
            updateExistingNFT(positionNFTLocal.policy, positionNFTLocal.adaValue, positionNFTLocal.price, positionNFTLocal.balance, positionNFTLocal.change30D, positionNFTLocal.lastUpdated)
        }
    }

    @Transaction
    fun insertOrUpdateNFTList(positions: List<PositionNFTLocal>) {
        positions.forEach { insertOrUpdateNFT(it) }
    }

    @Query("UPDATE positionNFT SET adaValue = :adaValue, price = :price, balance = :balance, change30D = :change30D, lastUpdated = :lastUpdated WHERE policy = :policy")
    fun updateExistingNFT(policy: String, adaValue: Double, price: Double, balance: Double, change30D: Double, lastUpdated: ZonedDateTime)

    // LP positions

    @Query("SELECT * FROM positionLP ORDER by adaValue DESC")
    fun getAllLPPositions() : List<PositionLPLocal>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertLP(positionLPLocal: PositionLPLocal): Long

    @Transaction
    fun insertOrUpdateLP(positionLPLocal: PositionLPLocal) {
        val id = insertLP(positionLPLocal)
        if (id == -1L) { // Insert failed, so update the existing entry
            updateExistingLP(
                positionLPLocal.ticker,
                positionLPLocal.adaValue,
                positionLPLocal.amountLP,
                positionLPLocal.tokenAAmount,
                positionLPLocal.tokenBAmount,
                positionLPLocal.lastUpdated,
            )
        }
    }

    @Transaction
    fun insertOrUpdateLPList(positions: List<PositionLPLocal>) {
        positions.forEach {
            insertOrUpdateLP(it) }
    }

    @Query("UPDATE positionLP SET adaValue = :adaValue, amountLP = :amountLP, tokenAAmount = :tokenAAmount, tokenBAmount = :tokenBAmount, lastUpdated = :lastUpdated WHERE ticker = :ticker")
    fun updateExistingLP(ticker: String, adaValue: Double, amountLP: Double, tokenAAmount: Double, tokenBAmount: Double, lastUpdated: ZonedDateTime)
}
