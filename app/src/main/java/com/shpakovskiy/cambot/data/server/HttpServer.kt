package com.shpakovskiy.cambot.data.server

interface HttpServer {

    suspend fun start(port: Int)
}