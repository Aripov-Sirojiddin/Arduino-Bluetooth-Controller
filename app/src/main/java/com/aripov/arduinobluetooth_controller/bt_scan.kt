package com.aripov.arduinobluetooth_controller

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.service.autofill.OnClickAction
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class bt_scan : AppCompatActivity(), RecyclerViewAdapter.OnItemClickListener  {
  //Activity for result launchers
  private lateinit var enableBTLauncher: ActivityResultLauncher<Intent>
  private lateinit var makeDiscoverableLauncher: ActivityResultLauncher<Intent>
  private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
  //Recycler view
  private lateinit var pairedRecyclerView: RecyclerView
  private lateinit var discoveredRecyclerView: RecyclerView

  private lateinit var pairedAdapter: RecyclerViewAdapter
  private lateinit var discoveredAdapter: RecyclerViewAdapter

  //Buttons
  private lateinit var scanBTN : Button

  //Progress bar
  private lateinit var progressBar : ProgressBar

  //Bluetooth
  private lateinit var bluetoothManager: BluetoothManager
  private lateinit var bluetoothAdapter: BluetoothAdapter

  //BT - Receiver
  private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val action: String = intent.action.toString()
      when (action) {
        BluetoothDevice.ACTION_FOUND -> {
          val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
          if (device != null) {
            if (ActivityCompat.checkSelfPermission(
                application,
                Manifest.permission.BLUETOOTH_CONNECT
              ) != PackageManager.PERMISSION_GRANTED
            ) {
              requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
              if (device.name != null && device.address != null) {
                val btDevice = DiscoveredBTDevice(device.name, device.address)
                if (!discoveredAdapter.containsDevice(btDevice)) {
                  discoveredAdapter.addDiscoveredDevice(btDevice)
                }
              }
            }
          }
        }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(R.layout.activity_bt_scan)
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }
    //Buttons
    scanBTN = findViewById(R.id.btnScan)
    scanBTN.setOnClickListener{
      startOrStopScan()
    }

    //Progress Bar
    progressBar = findViewById(R.id.progressBar)

    //Recycler View
    pairedAdapter = RecyclerViewAdapter(mutableListOf(), this)
    pairedRecyclerView = findViewById(R.id.pairedDevices)
    pairedRecyclerView.layoutManager = LinearLayoutManager(this)
    pairedRecyclerView.adapter = pairedAdapter

    discoveredAdapter = RecyclerViewAdapter(mutableListOf(), this)
    discoveredRecyclerView = findViewById(R.id.discoveredDevices)
    discoveredRecyclerView.layoutManager = LinearLayoutManager(this)
    discoveredRecyclerView.adapter = discoveredAdapter

    bluetoothManager = getSystemService(BluetoothManager::class.java)
    bluetoothAdapter = bluetoothManager.adapter

    requestPermissionLauncher = registerForActivityResult(
      ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
      if (!isGranted) {
        shouldShowRequestPermissionRationale("We need these permissions to connect.")
      }
    }

    enableBTLauncher = registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
      if(result.resultCode != Activity.RESULT_OK) {
        requestEnableBT()
      } else {
        Toast.makeText(application, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
      }
    }

    makeDiscoverableLauncher = registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ){ result: ActivityResult ->
      if(result.resultCode != Activity.RESULT_OK) {
        Toast.makeText(application, "Device is not discoverable", Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(application, "Device is discoverable", Toast.LENGTH_SHORT).show()
      }
    }

    requestAllPermissions()
    if(!bluetoothAdapter.isEnabled){
      requestEnableBT()
    } else {
      queryPairedDevices()
      val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
      registerReceiver(receiver, filter)
    }
  }
  private fun requestAllPermissions() {
    requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
    requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN)
    requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_ADVERTISE)
    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
  }
  private fun requestEnableBT() {
    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
      requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
    }
    enableBTLauncher.launch(enableBtIntent)
  }
  private fun startOrStopScan() {
    if(bluetoothAdapter.isEnabled) {
      if (scanBTN.text == "Start Scan") {
        if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_SCAN
          ) != PackageManager.PERMISSION_GRANTED
        ) {
          requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN)
        }
        bluetoothAdapter.startDiscovery()
        scanBTN.text = "Stop Scan"
        progressBar.visibility = View.VISIBLE
      } else {
        bluetoothAdapter.cancelDiscovery()
        scanBTN.text = "Start Scan"
        progressBar.visibility = View.GONE
      }
    } else {
      requestEnableBT()
    }
  }
  private fun queryPairedDevices() {
    if (ActivityCompat.checkSelfPermission(application, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
      requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
    }
    bluetoothAdapter.bondedDevices.forEach { device ->
      pairedAdapter.addDiscoveredDevice(
        DiscoveredBTDevice(
          device.name,
          device.address
      ))
    }
  }
  private fun setDeviceDiscoverable() {
    val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
      putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
    }
    makeDiscoverableLauncher.launch(discoverableIntent)
  }

  override fun onItemClick(position: Int) {
    setDeviceDiscoverable()
  }
  override fun onDestroy() {
    super.onDestroy()
    unregisterReceiver(receiver)
  }
}