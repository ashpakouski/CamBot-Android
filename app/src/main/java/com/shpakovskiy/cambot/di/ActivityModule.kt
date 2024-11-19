package com.shpakovskiy.cambot.di

import android.content.Context
import com.shpakovskiy.cambot.data.camera.CameraFrameFeedProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {

    @Provides
    fun provideCameraFeedProvider(context: Context): CameraFrameFeedProvider {
        return CameraFrameFeedProvider(context)
    }

//    @Provides
//    @ActivityContext
//    fun provideActivityContext(activity: Activity): Context {
//        return activity
//    }
}