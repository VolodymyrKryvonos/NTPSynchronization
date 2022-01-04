package com.example.ntpsynchronization

import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class SendReceive(private var socket: Socket?, private val handler: Handler) : Thread() {
    private var inputStream: InputStream? = socket?.getInputStream()
    private var outputStream: OutputStream? = socket?.getOutputStream()

    override fun run() {
        val buffer: ByteArray = ByteArray(1024)
        var bytes: Int
        while (socket != null) {
            try {
                bytes = inputStream?.read(buffer) ?: 0
                if (bytes > 0) {
                    handler.obtainMessage(1, bytes, -1, buffer).sendToTarget()
                }
            } catch (e: IOException) {
                Log.e("SendReceive", "Read failed ${e.localizedMessage}")
            }
        }
    }

    fun write(bytes: ByteArray) {
        try {
            outputStream?.write(bytes)
        } catch (e: IOException) {
            Log.e("SendReceive", "Write failed ${e.localizedMessage}")
        }
    }
}