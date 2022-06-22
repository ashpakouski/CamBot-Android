package com.shpakovskiy.cambot.di

import android.content.Context
import androidx.work.WorkManager
import com.shpakovskiy.cambot.bluetooth.BluetoothConnector
import com.shpakovskiy.cambot.data.LocalWebSocketServer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.net.InetSocketAddress
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    @Singleton
    fun provideWebSocketServer(): LocalWebSocketServer {
        val inetSocketAddress = InetSocketAddress("192.168.100.2", 9999)
        val webSocketServer = LocalWebSocketServer(inetSocketAddress)
        webSocketServer.isReuseAddr = true

        return LocalWebSocketServer(address = inetSocketAddress)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideBluetoothConnector(): BluetoothConnector {
        return BluetoothConnector()
    }
}