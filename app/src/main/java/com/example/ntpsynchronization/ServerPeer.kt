package com.example.ntpsynchronization

import android.os.Handler
import android.util.Log
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class ServerPeer(private val handler: Handler, private var sendReceive: SendReceive?) : Thread() {
    private var socket: Socket? = null
    private var serverSocket: ServerSocket? = null
    override fun run(){
        try {
            serverSocket = ServerSocket(8888)
            socket = serverSocket?.accept()
            sendReceive = SendReceive(socket, handler)
            sendReceive?.start()
        }catch (e: IOException){
            Log.e("ServerPeer", e.toString())
        }
    }
}