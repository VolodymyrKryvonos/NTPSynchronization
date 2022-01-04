package com.example.ntpsynchronization

import android.os.Handler
import android.util.Log
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class ClientPeer(
    private var hostAddress: InetAddress,
    private val handler: Handler,
    private var sendReceive: SendReceive?
) : Thread() {
    private var socket: Socket = Socket()
    override fun run() {
        try {
            socket.connect(InetSocketAddress(hostAddress, 8888), 500)

            sendReceive = SendReceive(socket, handler)
            sendReceive?.start()
        } catch (e: IOException) {
            Log.e("ServerPeer", e.toString())
        }
    }
}