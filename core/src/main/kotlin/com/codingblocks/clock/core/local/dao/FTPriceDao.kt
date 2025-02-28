package com.codingblocks.clock.core.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codingblocks.clock.core.local.data.FTPriceEntity
import com.codingblocks.clock.core.local.data.NFTStatsEntity

@Dao
interface FTPriceDao {
    @Insert
    suspend fun insert(ftPrice: FTPriceEntity)

    @Insert
    suspend fun insertAll(ftPrices: List<FTPriceEntity>)

    @Query("SELECT * FROM ft_prices WHERE unit = :unit AND timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getPricesForUnitInTimeRange(unit: String, startTime: Long, endTime: Long): List<FTPriceEntity>

    @Query("SELECT * FROM ft_prices WHERE unit = :unit ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestPricesForUnit(unit: String, limit: Int): List<FTPriceEntity>

    @Query("SELECT * FROM ft_prices WHERE unit = :unit ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestPriceForUnit(unit: String): FTPriceEntity?

    @Query("SELECT * FROM ft_prices WHERE unit = :unit AND timestamp >= :validTime ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestValidPricesForUnit(unit: String, validTime: Long, limit: Int): List<FTPriceEntity>?
}