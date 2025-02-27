package com.codingblocks.clock.core

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
class NFTAlertWorkerFactory(
    private val dataRepo: DataRepo
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return NFTAlertWorker(appContext, workerParameters, dataRepo)
    }
}