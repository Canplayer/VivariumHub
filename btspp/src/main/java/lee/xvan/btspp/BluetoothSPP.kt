/*
 * Copyright (C) 2014 Akexorcist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lee.xvan.btspp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import java.util.*


class BluetoothSPP(
    private val mContext: Context
) {
    private var mBluetoothStateListener: BluetoothStateListener? = null
    private var mDataReceivedListener: OnDataReceivedListener? = null
    private var mBluetoothConnectionListener: BluetoothConnectionListener? = null
    private var mAutoConnectionListener: AutoConnectionListener? = null

    // Local Bluetooth adapter
    var bluetoothAdapter: BluetoothAdapter? = null

    // Member object for the chat services
    private var mChatService: BluetoothService? = null

    // Name and Address of the connected device
    var connectedDeviceName: String? = null
        private set
    var connectedDeviceAddress: String? = null
        private set
    var isAutoConnecting = false
        private set
    private var isAutoConnectionEnabled = false
    private var isConnected = false
    private var isConnecting = false
    private var isServiceRunning = false
    private var keyword = ""
    private var isAndroid = BluetoothState.DEVICE_ANDROID
    private var bcl: BluetoothConnectionListener? = null
    private var c = 0

    interface BluetoothStateListener {
        fun onServiceStateChanged(state: Int)
    }

    interface OnDataReceivedListener {
        fun onDataReceived(data: ByteArray?, message: String?)
    }

    interface BluetoothConnectionListener {
        fun onDeviceConnected(name: String?, address: String?)
        fun onDeviceDisconnected()
        fun onDeviceConnectionFailed()
    }

    interface AutoConnectionListener {
        fun onAutoConnectionStarted()
        fun onNewConnection(name: String?, address: String?)
    }

    val isBluetoothAvailable: Boolean
        get() {
            try {
                if (bluetoothAdapter == null || bluetoothAdapter?.address == null) return false
            } catch (e: NullPointerException) {
                return false
            }
            return true
        }

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled ?: false

    val isServiceAvailable: Boolean
        get() = mChatService != null

    fun startDiscovery(): Boolean {
        return bluetoothAdapter?.startDiscovery() ?: false
    }

    val isDiscovery: Boolean
        get() = bluetoothAdapter?.isDiscovering ?: false

    fun cancelDiscovery(): Boolean {
        return bluetoothAdapter?.cancelDiscovery() ?: false
    }

//    fun setupService() {
//        mChatService = BluetoothService(mContext, mHandler)
//    }

    val serviceState: Int
        get() = if (mChatService != null) (mChatService?.state ?: -1) else -1

    fun startService(isAndroid: Boolean) {
        if (mChatService != null) {
            if (mChatService?.state == BluetoothState.STATE_NONE) {
                isServiceRunning = true
                mChatService!!.start(isAndroid)
                this.isAndroid = isAndroid
            }
        } else {
            mChatService = BluetoothService(mContext, mHandler)
        }
    }

    fun stopService() {
        isServiceRunning = false
        mChatService?.stop()
//        Handler().postDelayed({
//            isServiceRunning = false
//            mChatService?.stop()
//        }, 500)
    }

    fun setDeviceTarget(isAndroid: Boolean) {
        stopService()
        startService(isAndroid)
        this.isAndroid = isAndroid
    }

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BluetoothState.MESSAGE_WRITE -> {
                }
                BluetoothState.MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    val readMessage = String(readBuf)
                    if (readBuf.isNotEmpty()) {
                        if (mDataReceivedListener != null) mDataReceivedListener!!.onDataReceived(
                            readBuf,
                            readMessage
                        )
                    }
                }
                BluetoothState.MESSAGE_DEVICE_NAME -> {
                    connectedDeviceName = msg.data.getString(BluetoothState.DEVICE_NAME)
                    connectedDeviceAddress =
                        msg.data.getString(BluetoothState.DEVICE_ADDRESS)
                    if (mBluetoothConnectionListener != null) mBluetoothConnectionListener!!.onDeviceConnected(
                        connectedDeviceName,
                        connectedDeviceAddress
                    )
                    isConnected = true
                }
                BluetoothState.MESSAGE_TOAST -> {
                    Toast.makeText(
                        mContext, msg.data.getString(BluetoothState.TOAST)
                        , Toast.LENGTH_SHORT
                    ).show()
                }
                BluetoothState.MESSAGE_STATE_CHANGE -> {
                    if (mBluetoothStateListener != null) mBluetoothStateListener!!.onServiceStateChanged(
                        msg.arg1
                    )
                    if (isConnected && msg.arg1 != BluetoothState.STATE_CONNECTED) {
                        if (mBluetoothConnectionListener != null) mBluetoothConnectionListener!!.onDeviceDisconnected()
                        if (isAutoConnectionEnabled) {
                            isAutoConnectionEnabled = false
                            autoConnect(keyword)
                        }
                        isConnected = false
                        connectedDeviceName = null
                        connectedDeviceAddress = null
                    }
                    if (!isConnecting && msg.arg1 == BluetoothState.STATE_CONNECTING) {
                        isConnecting = true
                    } else if (isConnecting) {
                        if (msg.arg1 != BluetoothState.STATE_CONNECTED) {
                            if (mBluetoothConnectionListener != null) mBluetoothConnectionListener!!.onDeviceConnectionFailed()
                        }
                        isConnecting = false
                    }
                }
            }
        }
    }

    fun stopAutoConnect() {
        isAutoConnectionEnabled = false
    }

    fun connect(address: String?) {
        val device = bluetoothAdapter?.getRemoteDevice(address)
        if (device != null) {
            mChatService?.connect(device)
        } else {
            Log.d("INFO", "Trying to connect to an Empty Device")
        }
    }

    fun disconnect() {
        if (mChatService != null) {
            isServiceRunning = false
            mChatService?.stop()
            if (mChatService?.state == BluetoothState.STATE_NONE) {
                isServiceRunning = true
                mChatService?.start(isAndroid)
            }
        }
    }

    fun setBluetoothStateListener(listener: BluetoothStateListener?) {
        mBluetoothStateListener = listener
    }

    fun setOnDataReceivedListener(listener: OnDataReceivedListener?) {
        mDataReceivedListener = listener
    }

    fun setBluetoothConnectionListener(listener: BluetoothConnectionListener?) {
        mBluetoothConnectionListener = listener
    }

    fun setAutoConnectionListener(listener: AutoConnectionListener?) {
        mAutoConnectionListener = listener
    }

    fun enable() {
        bluetoothAdapter!!.enable()
    }

    fun send(data: ByteArray, CRLF: Boolean) {
        if (mChatService?.state == BluetoothState.STATE_CONNECTED) {
            if (CRLF) {
                val data2 = ByteArray(data.size + 2)
                for (i in data.indices) data2[i] = data[i]
                data2[data2.size - 2] = 0x0A
                data2[data2.size - 1] = 0x0D
                mChatService?.write(data2)
            } else {
                mChatService?.write(data)
            }
        }
    }

    fun send(data: String, CRLF: Boolean) {
        var mdata = data
        if (mChatService!!.state == BluetoothState.STATE_CONNECTED) {
            if (CRLF) mdata += "\r\n"
            mChatService!!.write(mdata.toByteArray())
        }
    }

    val pairedDeviceName: Array<String?>
        get() {
            val devices = bluetoothAdapter!!.bondedDevices
            val name_list = arrayOfNulls<String>(devices.size)
            for ((c, device) in devices.withIndex()) {
                name_list[c] = device.name
            }
            return name_list
        }

    val pairedDeviceAddress: Array<String?>
        get() {
            val devices = bluetoothAdapter!!.bondedDevices
            val address_list = arrayOfNulls<String>(devices.size)
            for ((c, device) in devices.withIndex()) {
                address_list[c] = device.address
            }
            return address_list
        }

    fun autoConnect(keywordName: String) {
        if (!isAutoConnectionEnabled) {
            keyword = keywordName
            isAutoConnectionEnabled = true
            isAutoConnecting = true
            if (mAutoConnectionListener != null) mAutoConnectionListener!!.onAutoConnectionStarted()
            val arr_filter_address =
                ArrayList<String?>()
            val arrFilterName =
                ArrayList<String?>()
            val arrName = pairedDeviceName
            val arrAddress = pairedDeviceAddress
            for (i in arrName.indices) {
                if (arrName[i]!!.contains(keywordName)) {
                    arr_filter_address.add(arrAddress[i])
                    arrFilterName.add(arrName[i])
                }
            }
            bcl = object : BluetoothConnectionListener {
                override fun onDeviceConnected(
                    name: String?,
                    address: String?
                ) {
                    bcl = null
                    isAutoConnecting = false
                }

                override fun onDeviceDisconnected() {}
                override fun onDeviceConnectionFailed() {
                    Log.e("CHeck", "Failed")
                    if (isServiceRunning) {
                        if (isAutoConnectionEnabled) {
                            c++
                            if (c >= arr_filter_address.size) c = 0
                            connect(arr_filter_address[c])
                            Log.e("CHeck", "Connect")
                            if (mAutoConnectionListener != null) mAutoConnectionListener!!.onNewConnection(
                                arrFilterName[c]
                                , arr_filter_address[c]
                            )
                        } else {
                            bcl = null
                            isAutoConnecting = false
                        }
                    }
                }
            }
            setBluetoothConnectionListener(bcl)
            c = 0
            if (mAutoConnectionListener != null) mAutoConnectionListener!!.onNewConnection(
                arrName[c], arrAddress[c]
            )
            if (arr_filter_address.size > 0) connect(arr_filter_address[c]) else Toast.makeText(
                mContext,
                "Device name mismatch",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    init {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }
}