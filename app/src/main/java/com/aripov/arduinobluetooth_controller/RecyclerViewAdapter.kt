package com.aripov.arduinobluetooth_controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAdapter(private val devicesList: MutableList<DiscoveredBTDevice>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val deviceNameView: TextView = itemView.findViewById(R.id.deviceName)
    val deviceMacView: TextView = itemView.findViewById(R.id.deviceMacAddress)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.discovered_bt_device, parent, false)
    return ViewHolder(view)
  }

  override fun getItemCount(): Int {
    return devicesList.size
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.deviceNameView.text = devicesList[position].name
    holder.deviceMacView.text = devicesList[position].mac
  }

  fun addDiscoveredDevice(discoveredBTDevice: DiscoveredBTDevice) {
    devicesList.add(discoveredBTDevice)
    notifyItemInserted(devicesList.size - 1)
  }

  fun containsDevice(discoveredBTDevice: DiscoveredBTDevice) : Boolean {
    return devicesList.contains(discoveredBTDevice)
  }
 }