package org.breezyweather.background.polling.work.worker

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.Keep
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.google.common.util.concurrent.ListenableFuture

abstract class AsyncWorker @Keep @SuppressLint("BanKeepAnnotation") constructor(
    context: Context,
    workerParams: WorkerParameters
) : ListenableWorker(context, workerParams) {

    abstract fun doAsyncWork(future: SettableFuture<Result>)

    @SuppressLint("RestrictedApi")
    override fun startWork(): ListenableFuture<Result> {
        // Package-private to avoid synthetic accessor.
        val future = SettableFuture.create<Result>()
        doAsyncWork(future)
        return future
    }
}
