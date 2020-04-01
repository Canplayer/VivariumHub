package com.canplayer.vivariumhub

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private val REQUEST_ENABLE_BT = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn_search).setOnClickListener { this.onSearchClicked() }
    }

    private fun onSearchClicked() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!mBluetoothAdapter.isEnabled) {
            Log.d("INFO", "Bluetooth is off! Tuning on!")
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            Log.d("INFO", "Bluetooth is on!")
            this.startSearching()
        }
    }

    fun startSearching() {
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, filter)
        mBluetoothAdapter.startDiscovery()
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("INFO", "ACTION_DISCOVERY_STARTED")
                    Log.d("INFO", "Searching:" + mBluetoothAdapter.isDiscovering)
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("INFO", "ACTION_DISCOVERY_FINISHED")
                    Log.d("INFO", "Searching:" + mBluetoothAdapter.isDiscovering)
                }
                BluetoothDevice.ACTION_FOUND -> {
                    Log.d("INFO", "BluetoothDevice ACTION_FOUND")
                }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            Log.d("INFO", "resultCode: $resultCode")
            Log.d("INFO", "blueTooth is on: " + mBluetoothAdapter.isEnabled)
            if (mBluetoothAdapter.isEnabled) {
                this.startSearching()
            }
        }
    }
}
