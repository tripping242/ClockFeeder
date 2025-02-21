package com.codingblocks.clock.core.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.codingblocks.clock.core.local.data.FeedFT
import com.codingblocks.clock.core.local.data.FeedNFT

@Dao
interface FeedFTDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(feedFT: FeedFT): Long

    @Update
    suspend fun update(feedFT: FeedFT)

    @Delete
    suspend fun delete(feedFT: FeedFT)

    @Query("SELECT * FROM feedFT WHERE positionUnit = :unit")
    suspend fun getFeedByPositionUnit(unit: String): FeedFT?

    @Query("SELECT * FROM feedFT ORDER by lastUpdatedAt DESC")
    suspend fun getAllFeedFT(): List<FeedFT>

    @Query("DELETE FROM feedFT WHERE positionUnit = :unit")
    suspend fun deleteByPositionUnit(unit: String)

    @Transaction
    suspend fun replaceReferencedPositionFTLocal(oldUnit: String, newUnit: String) {
        val feed = getFeedByPositionUnit(oldUnit)
        if (feed != null) {
            delete(feed)
            val newFeed = feed.copy(positionUnit = newUnit)
            insert(newFeed)
        }
    }
}

@Dao
interface FeedNFTDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(feedNFT: FeedNFT): Long

    @Update
    suspend fun update(feedNFT: FeedNFT)

    @Delete
    suspend fun delete(feedNFT: FeedNFT)

    @Query("SELECT * FROM feedNFT WHERE positionPolicy = :policy")
    suspend fun getFeedByPositionPolicy(policy: String): FeedNFT?

    @Query("SELECT * FROM feedNFT ORDER by lastUpdatedAt DESC")
    suspend fun getAllFeedNFT(): List<FeedNFT>

    @Query("DELETE FROM feedNFT WHERE positionPolicy = :policy")
    suspend fun deleteByPositionPolicy(policy: String)

    @Transaction
    suspend fun replaceReferencedPositionNFTLocal(oldPolicy: String, newPolicy: String) {
        val feed = getFeedByPositionPolicy(oldPolicy)
        if (feed != null) {
            delete(feed)
            val newFeed = feed.copy(positionPolicy = newPolicy)
            insert(newFeed)
        }
    }


}