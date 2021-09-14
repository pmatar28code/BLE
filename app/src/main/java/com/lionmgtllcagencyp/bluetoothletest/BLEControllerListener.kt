package com.lionmgtllcagencyp.bluetoothletest


interface BLEControllerListener {
    fun BLEControllerConnected()
    fun BLEControllerDisconnected()
    fun BLEDeviceFound(name: String?, address: String?)
}