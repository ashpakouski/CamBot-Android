package com.shpakovskiy.cambot.data

import android.content.res.AssetManager
import com.shpakovskiy.cambot.common.LOCAL_WEB_SERVER_PORT
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

class LocalWebServer(private val assetManager: AssetManager) {
    val server by lazy {
        embeddedServer(Netty, LOCAL_WEB_SERVER_PORT, watchPaths = emptyList()) {
            routing {
                get("/") {
                    val pageContent = assetManager.open("index.html").readBytes()
                    call.respondBytes(
                        bytes = pageContent,
                        contentType = ContentType.Text.Html
                    )
                }
                get("/image/{imageName}") {
                    val imageName = call.parameters["imageName"]
                    val imageBytes = assetManager.open("images/$imageName").readBytes()

                    call.response.header(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.Attachment.withParameter(
                            ContentDisposition.Parameters.FileName,
                            "$imageName"
                        ).toString()
                    )
                    call.respondBytes(imageBytes)
                }
            }
        }
    }
}