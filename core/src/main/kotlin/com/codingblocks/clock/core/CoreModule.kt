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

import com.codingblocks.clock.core.local.localModule
import com.codingblocks.clock.core.model.AppBuildInfo
import com.codingblocks.clock.core.remote.remoteModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module

internal val coreModule = module {

    factory { HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY } }

    single { provideOkHttpClient(loggingInterceptor = get(), appBuildInfo = get()) }

    single<DataRepo> {
        CoreDataRepo(
            context = get(),
            database = get(),
            okHttpClient = get(),
            appBuildInfo = get(),
        ) }
}

val coreModules = listOf(coreModule, localModule, remoteModule)

private fun provideOkHttpClient(
    loggingInterceptor: HttpLoggingInterceptor,
    appBuildInfo: AppBuildInfo,
): OkHttpClient = OkHttpClient()
    .newBuilder()
    .apply {
        if (appBuildInfo.debug) addInterceptor(loggingInterceptor)
    }
    .build()