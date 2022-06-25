package com.shpakovskiy.cambot.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.util.Log
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
    fun provideWebSocketServer(@ApplicationContext context: Context): LocalWebSocketServer {
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val linkProperties = connectivityManager.getLinkProperties(
            connectivityManager.activeNetwork
        ) as LinkProperties
        // Log.d("TAG123", "Address: ${linkProperties.linkAddresses.last().address}")
        val server = LocalWebSocketServer(
            InetSocketAddress(linkProperties.linkAddresses.last().address, 9999)
        )
        server.isReuseAddr = true
        return server
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