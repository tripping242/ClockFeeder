/*
 * Copyright 2020 Tailored Media GmbH.
 * Created by Florian Schuster.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codingblocks.clock.core.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.codingblocks.clock.core.local.dao.CustomFTAlertDao
import com.codingblocks.clock.core.local.dao.CustomNFTAlertDao
import com.codingblocks.clock.core.local.dao.FTPriceDao
import com.codingblocks.clock.core.local.dao.FeedFTDao
import com.codingblocks.clock.core.local.dao.FeedNFTDao
import com.codingblocks.clock.core.local.dao.NFTStatsDao
import com.codingblocks.clock.core.local.dao.PositionsDao
import com.codingblocks.clock.core.local.dao.TokenLogoDao
import com.codingblocks.clock.core.local.dao.WatchListsDao
import com.codingblocks.clock.core.local.data.CustomFTAlert
import com.codingblocks.clock.core.local.data.CustomNFTAlert
import com.codingblocks.clock.core.local.data.FTPriceEntity
import com.codingblocks.clock.core.local.data.FeedFT
import com.codingblocks.clock.core.local.data.FeedNFT
import com.codingblocks.clock.core.local.data.NFTStatsEntity
import com.codingblocks.clock.core.local.data.PositionFTLocal
import com.codingblocks.clock.core.local.data.PositionLPLocal
import com.codingblocks.clock.core.local.data.PositionNFTLocal
import com.codingblocks.clock.core.local.data.TokenLogo
import com.codingblocks.clock.core.local.data.WatchListConfig


@Database(
    entities = [PositionFTLocal::class, PositionNFTLocal::class, PositionLPLocal::class, WatchListConfig::class, FeedFT:: class, FeedNFT::class, CustomFTAlert::class, CustomNFTAlert::class, TokenLogo::class, NFTStatsEntity::class, FTPriceEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(ZonedDateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val NAME = "TapTools_DB"
    }

    abstract fun getPositionsDao(): PositionsDao
    abstract fun getWatchListsDao(): WatchListsDao
    abstract fun getFeedFTDao(): FeedFTDao
    abstract fun getFeedNFTDao(): FeedNFTDao
    abstract fun getFTAlertsDao(): CustomFTAlertDao
    abstract fun getNFTAlertsDao(): CustomNFTAlertDao
    abstract fun getLogoDao(): TokenLogoDao
    abstract fun getNFTStatsDao(): NFTStatsDao
    abstract fun getFTPriceDao(): FTPriceDao

}



