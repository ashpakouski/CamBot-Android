package com.shpakovskiy.cambot

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CamBotApp : Application() {

//    @Inject
//    lateinit var workerFactory: HiltWorkerFactory
//
//    override fun getWorkManagerConfiguration(): Configuration {
//        return Configuration.Builder()
//            .setWorkerFactory(workerFactory)
//            .build()
//    }
}