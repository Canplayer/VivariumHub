package lee.xvan.btspp

object BluetoothState {
    // Constants that indicate the current connection state
    const val STATE_NONE = 0 // we're doing nothing
    const val STATE_LISTEN = 1 // now listening for incoming connections
    const val STATE_CONNECTING = 2 // now initiating an outgoing connection
    const val STATE_CONNECTED = 3 // now connected to a remote device
    const val STATE_NULL = -1 // now service is null
    // Message types sent from the BluetoothChatService Handler
    const val MESSAGE_STATE_CHANGE = 1
    const val MESSAGE_READ = 2
    const val MESSAGE_WRITE = 3
    const val MESSAGE_DEVICE_NAME = 4
    const val MESSAGE_TOAST = 5
    // Intent request codes
    const val REQUEST_CONNECT_DEVICE = 384
    const val REQUEST_ENABLE_BT = 385
    // Key names received from the BluetoothChatService Handler
    const val DEVICE_NAME = "device_name"
    const val DEVICE_ADDRESS = "device_address"
    const val TOAST = "toast"
    const val DEVICE_ANDROID = true
    const val DEVICE_OTHER = false
    // Return Intent extra
    var EXTRA_DEVICE_ADDRESS = "device_address"
}