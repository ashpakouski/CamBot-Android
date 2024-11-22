package com.shpakovskiy.cambot.data.server

import android.util.Log
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class KtorWebSocketServer @Inject constructor() : WebSocketServer {

    private var activeSession = AtomicReference<DefaultWebSocketServerSession?>(null)

    override suspend fun start(port: Int) {
        withContext(Dispatchers.IO) {
            embeddedServer(
                Netty,
                port = port,
                module = { module() }
            ).start(wait = true)
        }
    }

    override suspend fun broadcastBinary(bytes: ByteArray) {
        //withContext(Dispatchers.IO) {
            activeSession.get()?.send(Frame.Binary(true, bytes))
        //}
    }

    private fun Application.module() {
        install(WebSockets)

        routing {
            webSocket("/") {
                activeSession.set(this)

                while (true) {
                    delay(1000L)
                }
            }
        }
    }
}