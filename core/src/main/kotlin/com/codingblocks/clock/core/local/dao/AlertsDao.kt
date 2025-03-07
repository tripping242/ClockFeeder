package com.codingblocks.clock.core.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.codingblocks.clock.core.local.data.CustomFTAlert
import com.codingblocks.clock.core.local.data.CustomNFTAlert

@Dao
interface CustomFTAlertDao {
    @Insert
    suspend fun insert(alert: CustomFTAlert)

    @Update
    suspend fun update(alert: CustomFTAlert)

    @Delete
    suspend fun delete(alert: CustomFTAlert)

    @Query("SELECT * FROM customFTAlert WHERE feedPositionUnit = :positionUnit ORDER BY threshold DESC")
    suspend fun getAlertsForFeed(positionUnit: String): List<CustomFTAlert>

    @Query("DELETE FROM customFTAlert WHERE feedPositionUnit = :positionUnit")
    suspend fun deleteAlertsForFeed(positionUnit: String)

    @Query("SELECT * FROM CustomFTAlert WHERE isEnabled = 1")
    suspend fun getAllEnabledAlerts(): List<CustomFTAlert>

}

@Dao
interface CustomNFTAlertDao {
    @Insert
    suspend fun insert(alert: CustomNFTAlert)

    @Update
    suspend fun update(alert: CustomNFTAlert)

    @Delete
    suspend fun delete(alert: CustomNFTAlert)

    @Query("SELECT * FROM customNFTAlert WHERE feedPositionPolicy = :positionPolicy ORDER BY threshold DESC")
    suspend fun getAlertsForFeed(positionPolicy: String): List<CustomNFTAlert>

    @Query("DELETE FROM customNFTAlert WHERE feedPositionPolicy = :positionPolicy")
    suspend fun deleteAlertsForFeed(positionPolicy: String)

    @Query("SELECT * FROM CustomNFTAlert WHERE isEnabled = 1")
    suspend fun getAllEnabledAlerts(): List<CustomNFTAlert>

}