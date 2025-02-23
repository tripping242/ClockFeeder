package com.codingblocks.clock.core.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.codingblocks.clock.core.local.data.WatchListConfig
import com.codingblocks.clock.core.local.data.WatchlistWithPositions

@Dao
interface WatchListsDao {
    @Insert
    fun insertWatchlist(watchListConfig: WatchListConfig): Long
    @Query("DELETE FROM watchListConfig WHERE watchlistNumber = :watchlistNumber")
    fun deleteWatchlistById(watchlistNumber: Int)
    @Query("SELECT * FROM watchListConfig WHERE watchlistNumber = :watchlistNumber")
    fun getWatchlistById(watchlistNumber: Int): WatchListConfig?
    @Query("SELECT * FROM watchListConfig ORDER BY createdAt DESC")
    fun getAllWatchlists(): List<WatchListConfig>

    @Update
    fun updateWatchListSettingsDb(watchListConfig: WatchListConfig)

    // merged object with all watchlists containing there positionLists
    @Transaction
    @Query("SELECT * FROM watchListConfig")
    fun getWatchlistsWithPositions(): List<WatchlistWithPositions>

}