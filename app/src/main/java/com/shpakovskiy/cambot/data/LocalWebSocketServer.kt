package com.shpakovskiy.cambot.data

import android.util.Log
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress
import java.util.*

class LocalWebSocketServer(address: InetSocketAddress) : WebSocketServer(address) {
    companion object {
        private const val TAG = "WebSocketServer"
    }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        Log.d(TAG, "onOpen: ${conn?.remoteSocketAddress}")
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        Log.d(TAG, "onMessage: $message")
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        Log.e(TAG, "onError: ${Arrays.toString(ex?.stackTrace)}")
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        Log.e(TAG, "onClose")
    }
}