package com.codingblocks.clock.core.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codingblocks.clock.core.local.data.TokenLogo

@Dao
interface TokenLogoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogo(logo: TokenLogo)

    @Query("SELECT * FROM logos WHERE policyId = :policyId")
    suspend fun getLogo(policyId: String): TokenLogo?
}