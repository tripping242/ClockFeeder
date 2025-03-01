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

package com.codingblocks.clock.core

import androidx.room.Room
import androidx.work.WorkManager
import com.codingblocks.clock.core.local.AppDatabase
import com.codingblocks.clock.core.model.AppBuildInfo
import com.codingblocks.clock.core.remote.remoteModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.dsl.module

object NotificationActions {
    const val ACTION_OPEN_FEEDS = "com.codingblocks.clock.core.OPEN_FEEDS"
}

internal val coreModule = module {

    factory { HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY } }

    single { provideOkHttpClient(loggingInterceptor = get(), appBuildInfo = get()) }

    single { NotificationHelper(context = get()) }
    single {
        Room.databaseBuilder(
            context = get(),
            AppDatabase::class.java,
            AppDatabase.NAME,
            // temp test solution, replace with flow!!
        ).allowMainThreadQueries().build()
    }

    single { get<AppDatabase>().getPositionsDao() }
    single { WorkManager.getInstance(androidContext()) }


    single<DataRepo> {
        CoreDataRepo(
            context = get(),
            database = get(),
            okHttpClient = get(),
            appBuildInfo = get(),
            workManager = get(),
            notificationHelper = get(),
        )
    }

    single {
        FeedCycler(
            dataRepo = get()
        )
    }

    workerOf(::FetchPricesWorker)
    workerOf(::NFTAlertWorker)
    workerOf(::FTAlertWorker)
}

val coreModules = listOf(coreModule, remoteModule)

private fun provideOkHttpClient(
    loggingInterceptor: HttpLoggingInterceptor,
    appBuildInfo: AppBuildInfo,
): OkHttpClient = OkHttpClient()
    .newBuilder()
    .apply {
        if (appBuildInfo.debug) addInterceptor(loggingInterceptor)
    }
    .build()