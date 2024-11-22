package com.shpakovskiy.cambot.di

import com.shpakovskiy.cambot.data.server.KtorWebSocketServer
import com.shpakovskiy.cambot.data.server.WebSocketServer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkingModule {

    @Binds
    @Singleton
    abstract fun bindWebSocketServer(
        webSocketServer: KtorWebSocketServer
    ): WebSocketServer
}