package com.codingblocks.clock.core.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codingblocks.clock.core.local.data.FTPriceEntity

@Dao
interface FTPriceDao {
    @Insert
    suspend fun insert(ftPrice: FTPriceEntity)

    @Insert
    suspend fun insertAll(ftPrices: List<FTPriceEntity>)

    @Query("SELECT * FROM ft_prices WHERE unit = :unit AND timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getPricesForUnitInTimeRange(unit: String, startTime: Long, endTime: Long): List<FTPriceEntity>
}