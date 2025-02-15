package com.codingblocks.clock.core.local.dao

import androidx.room.Dao
import com.codingblocks.clock.core.local.data.PositionFT
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.codingblocks.clock.core.local.data.PositionLP
import com.codingblocks.clock.core.local.data.PositionNFT
import timber.log.Timber
import java.time.ZonedDateTime

@Dao
interface PositionsDao {
    // FT Positions, ticker is the name, fingerprint is unique

    @Query("SELECT * FROM positionFT ORDER by adaValue DESC")
    fun getAllFTPositions() : List<PositionFT>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFT(positionFT: PositionFT): Long

    @Transaction
    fun insertOrUpdateFT(positionFT: PositionFT) {
        val id = insertFT(positionFT)
        if (id == -1L) { // Insert failed, so update the existing entry
            updateExistingFT(
                positionFT.fingerprint,
                positionFT.adaValue,
                positionFT.price,
                positionFT.balance,
                positionFT.change30D,
                positionFT.lastUpdated,
            )
        }
    }

    @Transaction
    fun insertOrUpdateFTList(positions: List<PositionFT>) {
        positions.forEach {
            insertOrUpdateFT(it) }
    }

    @Query("UPDATE positionFT SET adaValue = :adaValue, price = :price, balance = :balance, change30D = :change30D, lastUpdated = :lastUpdated WHERE fingerprint = :fingerprint")
    fun updateExistingFT(fingerprint: String, adaValue: Double, price: Double, balance: Double, change30D: Double, lastUpdated: ZonedDateTime)

    // NFT Positions, name is the name, policy is unique

    @Query("SELECT * FROM positionNFT ORDER by adaValue DESC")
    fun getAllNFTPositions() : List<PositionNFT>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNFT(positionNFT: PositionNFT): Long

    @Update
    fun updateNFT(positionNFT: PositionNFT)

    @Transaction
    fun insertOrUpdateNFT(positionNFT: PositionNFT) {
        val id = insertNFT(positionNFT)
        if (id == -1L) { // Insert failed, so update the existing entry
            updateExistingNFT(positionNFT.policy, positionNFT.adaValue, positionNFT.price, positionNFT.balance, positionNFT.change30D, positionNFT.lastUpdated)
        }
    }

    @Transaction
    fun insertOrUpdateNFTList(positions: List<PositionNFT>) {
        positions.forEach { insertOrUpdateNFT(it) }
    }

    @Query("UPDATE positionNFT SET adaValue = :adaValue, price = :price, balance = :balance, change30D = :change30D, lastUpdated = :lastUpdated WHERE policy = :policy")
    fun updateExistingNFT(policy: String, adaValue: Double, price: Double, balance: Double, change30D: Double, lastUpdated: ZonedDateTime)

    // LP positions

    @Query("SELECT * FROM positionLP ORDER by adaValue DESC")
    fun getAllLPPositions() : List<PositionLP>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertLP(positionLP: PositionLP): Long

    @Transaction
    fun insertOrUpdateLP(positionLP: PositionLP) {
        val id = insertLP(positionLP)
        if (id == -1L) { // Insert failed, so update the existing entry
            updateExistingLP(
                positionLP.ticker,
                positionLP.adaValue,
                positionLP.amountLP,
                positionLP.tokenAAmount,
                positionLP.tokenBAmount,
                positionLP.lastUpdated,
            )
        }
    }

    @Transaction
    fun insertOrUpdateLPList(positions: List<PositionLP>) {
        positions.forEach {
            insertOrUpdateLP(it) }
    }

    @Query("UPDATE positionLP SET adaValue = :adaValue, amountLP = :amountLP, tokenAAmount = :tokenAAmount, tokenBAmount = :tokenBAmount, lastUpdated = :lastUpdated WHERE ticker = :ticker")
    fun updateExistingLP(ticker: String, adaValue: Double, amountLP: Double, tokenAAmount: Double, tokenBAmount: Double, lastUpdated: ZonedDateTime)
}
