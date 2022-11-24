package com.dreamreco.firebaseappstreamtest.service

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters

class MyWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): ListenableWorker.Result {
        Log.e(TAG, "FireBase 메세지가 워크매니저를 거침")
        // TODO(developer): 시간이 오래 걸리는 업무는 여기서 처리
        return ListenableWorker.Result.success()
    }

    companion object {
        private val TAG = "MyWorker"
    }
}