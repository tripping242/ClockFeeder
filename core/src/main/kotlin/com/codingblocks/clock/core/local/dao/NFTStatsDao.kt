package com.codingblocks.clock.core.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.codingblocks.clock.core.local.data.NFTStatsEntity

@Dao
interface NFTStatsDao {
    @Insert
    suspend fun insert(nftStats: NFTStatsEntity)

    @Query("SELECT * FROM nft_stats WHERE policy = :policy AND timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getStatsForPolicyInTimeRange(policy: String, startTime: Long, endTime: Long): List<NFTStatsEntity>

    @Query("SELECT * FROM nft_stats WHERE policy = :policy ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestStatsForPolicy(policy: String, limit: Int): List<NFTStatsEntity>
}