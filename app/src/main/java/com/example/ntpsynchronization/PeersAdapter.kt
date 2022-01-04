package com.example.ntpsynchronization

import android.net.wifi.p2p.WifiP2pDevice
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


import android.view.LayoutInflater




class PeersAdapter(private val peerClick: (WifiP2pDevice)->Unit): RecyclerView.Adapter<PeersAdapter.PeerViewHolder>() {

    var peerList: ArrayList<WifiP2pDevice> = arrayListOf()

    override fun onBindViewHolder(holder: PeerViewHolder, position: Int) {
        holder.peerName?.text = peerList[position].deviceName
        holder.itemView.setOnClickListener{
            peerClick(peerList[position])
        }
    }

    override fun getItemCount() = peerList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.peer_item, parent, false)
        return PeerViewHolder(v)
    }

    inner class PeerViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var peerName: TextView? = null
        init {
            peerName = view.findViewById(R.id.peer_name)
        }
    }
}