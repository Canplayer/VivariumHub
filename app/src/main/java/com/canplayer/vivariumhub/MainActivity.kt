package com.canplayer.vivariumhub

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import lee.xvan.btspp.BluetoothSPP
import pub.devrel.easypermissions.EasyPermissions


const val RC_LOCATION_REQ = 3

class MainActivity : AppCompatActivity() {
    lateinit var bt: BluetoothSPP
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bt = BluetoothSPP(this)
        findViewById<Button>(R.id.btn_search).setOnClickListener { this.onSearchClicked() }
    }

    private fun onSearchClicked() {
        if (!bt.isBluetoothAvailable) {
            Log.d("INFO", "bt.isBluetoothAvailable false")
        } else
            Log.d("INFO", "bt.isBluetoothAvailable on")
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, filter)
        bt.startDiscovery()
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("INFO", "ACTION_DISCOVERY_STARTED")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("INFO", "ACTION_DISCOVERY_FINISHED")
                }
                BluetoothDevice.ACTION_FOUND -> {
                    Log.d("INFO", "BluetoothDevice ACTION_FOUND")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}
