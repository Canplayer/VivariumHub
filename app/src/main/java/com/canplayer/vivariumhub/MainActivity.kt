package com.canplayer.vivariumhub

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.inuker.bluetooth.library.BluetoothClient
import com.inuker.bluetooth.library.beacon.Beacon
import com.inuker.bluetooth.library.search.SearchRequest
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import com.inuker.bluetooth.library.utils.BluetoothLog
import pub.devrel.easypermissions.EasyPermissions
import java.util.*


const val RC_LOCATION_REQ = 3

class MainActivity : AppCompatActivity() {
    lateinit var mClient: BluetoothClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mClient = BluetoothClient(this)
        findViewById<Button>(R.id.btn_search).setOnClickListener { this.onSearchClicked() }
    }

    private fun onSearchClicked() {
        val request = SearchRequest.Builder()
            .searchBluetoothClassicDevice(5000)
            .build()
        mClient.search(request, object : SearchResponse {
            override fun onSearchCanceled() {
            }

            override fun onDeviceFounded(device: SearchResult?) {
                Log.d("INFO", "Device: " + device?.name)
                val beacon = Beacon(device?.scanRecord)
                BluetoothLog.v(
                    java.lang.String.format(
                        "beacon for %s\n%s",
                        device?.address,
                        beacon.toString()
                    )
                )
            }

            override fun onSearchStarted() {
            }

            override fun onSearchStopped() {
            }
        })
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
