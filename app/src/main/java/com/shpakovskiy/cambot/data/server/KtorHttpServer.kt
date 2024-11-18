package com.shpakovskiy.cambot.data.server

import android.content.Context
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KtorHttpServer(private val context: Context) : HttpServer {

    override suspend fun start(port: Int) {
        withContext(Dispatchers.IO) {
            embeddedServer(
                Netty,
                port = port,
                module = { module() }
            ).start()
        }
    }

    private fun Application.module() {
        routing {
            get("/") {
                val pageContent = context.assets.open("index.html").readBytes()
                call.respondBytes(
                    bytes = pageContent,
                    contentType = ContentType.Text.Html
                )
            }
        }
    }
}