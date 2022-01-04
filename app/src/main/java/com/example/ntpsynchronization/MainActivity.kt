package com.example.ntpsynchronization

import android.Manifest
import android.content.BroadcastReceiver
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context

import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception
import android.net.wifi.p2p.WifiP2pManager

import android.net.wifi.WifiManager

class MainActivity : AppCompatActivity(), WifiP2pManager.ChannelListener, DeviceActionListener {

    private val TAG = "MainActivity"

    private val intentFilter = IntentFilter()
    private var receiver: BroadcastReceiver? = null
    private var wifiP2pManager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null

    var isWifiP2pEnabled = false
    private var retryChannel = false
    private fun initP2p(): Boolean {
        // Device capability definition check
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Log.e(TAG, "Wi-Fi Direct is not supported by this device.")
            return false
        }
        // Hardware capability check
        val wifiManager = getSystemService(WIFI_SERVICE) as? WifiManager
        if (wifiManager == null) {
            Log.e(TAG, "Cannot get Wi-Fi system service.")
            return false
        }
        if (!wifiManager.isP2pSupported) {
            Log.e(TAG, "Wi-Fi Direct is not supported by the hardware or Wi-Fi is off.")
            return false
        }
        wifiP2pManager = getSystemService(WIFI_P2P_SERVICE) as? WifiP2pManager
        if (wifiP2pManager == null) {
            Log.e(TAG, "Cannot get Wi-Fi Direct system service.")
            return false
        }
        channel = wifiP2pManager?.initialize(this, mainLooper, null)
        if (channel == null) {
            Log.e(TAG, "Cannot initialize Wi-Fi Direct.")
            return false
        }
        return true
    }

    private var send: Button? = null

    private var peerList: RecyclerView? = null
    private var adapter: PeersAdapter? = null
    private val peers: ArrayList<WifiP2pDevice> = arrayListOf()
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
            }
        }
    }

    private var clientPeer: ClientPeer? = null
    private var serverPeer: ServerPeer? = null
    private var sendReceive: SendReceive? = null

    val connectionInfoListener: WifiP2pManager.ConnectionInfoListener =
        WifiP2pManager.ConnectionInfoListener {
            if (it.groupFormed && it.isGroupOwner){
                serverPeer = ServerPeer(handler,sendReceive)
                serverPeer?.start()
            }else if (it.groupFormed){
                clientPeer = ClientPeer(it.groupOwnerAddress,handler,sendReceive)
                clientPeer?.start()
            }
        }


    val peerListListener = WifiP2pManager.PeerListListener {
        if (!it.deviceList.equals(peers)) {
            peers.clear()
            peers.addAll(it.deviceList)
            adapter?.peerList = peers
            adapter?.notifyDataSetChanged()
        }
    }

    private val handler = Handler(Looper.getMainLooper(), Handler.Callback {
        Log.e("Handler",it.arg1.toString())
        when (it.what) {
            1 -> {
                val readBuffer = it.obj as ByteArray
                val msg = String(readBuffer, 0, it.arg1)
                Toast.makeText(this, msg,Toast.LENGTH_LONG)
            }
        }
        return@Callback true
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        if (!initP2p()) {
            finish();
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }
        send = findViewById(R.id.send)
        peerList = findViewById(R.id.device_list)
        adapter = PeersAdapter {
            val config = WifiP2pConfig()
            config.deviceAddress = it.deviceAddress
            Log.e("TryConnect: ", it.deviceName + " " + it.deviceAddress)
            wifiP2pManager?.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Toast.makeText(
                        this@MainActivity,
                        "Connected to ${it.deviceName}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onFailure(p0: Int) {
                    Toast.makeText(this@MainActivity, "Can't connect", Toast.LENGTH_LONG).show()
                }
            })
        }

        send?.setOnClickListener {
            sendReceive?.write("Send from ${Build.MODEL}".toByteArray(Charsets.UTF_8))
        }

        peerList?.adapter = adapter
        peerList?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        wifiP2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = wifiP2pManager?.initialize(this, mainLooper, null)
        receiver = WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

    }

    fun resetData() {
        val fragmentList: DeviceListFragment = supportFragmentManager
            .findFragmentById(R.id.frag_list) as DeviceListFragment
        val fragmentDetails: DeviceDetailFragment = supportFragmentManager
            .findFragmentById(R.id.frag_detail) as DeviceDetailFragment
        if (fragmentList != null) {
            fragmentList.clearPeers()
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews()
        }
    }

    override fun onResume() {
        super.onResume()
        receiver = WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this)
        registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            Log.e("onPause", e.toString())
        }
    }

    override fun onChannelDisconnected() {
        TODO("Not yet implemented")
    }
}