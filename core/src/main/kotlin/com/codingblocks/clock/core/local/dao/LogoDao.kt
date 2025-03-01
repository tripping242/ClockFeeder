package com.codingblocks.clock.core.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codingblocks.clock.core.local.data.NFTLogo
import com.codingblocks.clock.core.local.data.TokenLogo

@Dao
interface TokenLogoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogo(logo: TokenLogo)

    @Query("SELECT * FROM token_logos WHERE unit = :unit")
    suspend fun getTokenLogo(unit: String): TokenLogo?
}

@Dao
interface NFTLogoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogo(logo: NFTLogo)

    @Query("SELECT * FROM nft_logos WHERE policy = :policy")
    suspend fun getNFTLogo(policy: String): NFTLogo?
}