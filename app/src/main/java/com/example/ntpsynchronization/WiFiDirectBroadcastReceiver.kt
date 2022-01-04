package com.example.ntpsynchronization

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat


class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager?,
    private val channel: WifiP2pManager.Channel?,
    private val activity: MainActivity
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        val action = intent.action
        when {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION == action -> {
                Log.e("WiFiDirectBroadcastReceiver", "WIFI_P2P_STATE_CHANGED_ACTION")
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // Wifi Direct mode is enabled
                    Toast.makeText(context, "WIFI IS ENABLED", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "WIFI IS DISABLED", Toast.LENGTH_LONG).show()
                }
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION == action -> {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                manager?.requestPeers(channel, activity.peerListListener)
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION == action -> {

                manager?.requestConnectionInfo(channel,activity.connectionInfoListener)

            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION == action -> {

            }
        }
    }

}