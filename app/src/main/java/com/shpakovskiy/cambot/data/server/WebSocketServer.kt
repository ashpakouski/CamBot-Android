package com.shpakovskiy.cambot.data.server

interface WebSocketServer {

    suspend fun start(port: Int)

    suspend fun broadcastBinary(bytes: ByteArray)
}