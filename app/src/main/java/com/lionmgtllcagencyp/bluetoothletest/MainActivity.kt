package com.lionmgtllcagencyp.bluetoothletest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity(){
    private var scanner: BluetoothLeScanner? = null
    private var listOfdevices = mutableListOf<ScanResult>()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()
        checkBLESupport()

        val bleCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                Log.e("DEVICE FROM SCAN","${result.device.address}, ${result.device.name}, ${result.device.uuids}")

            }


            override fun onBatchScanResults(results: List<ScanResult>) {
                for (sr in results) {
                   // val devices = results
                    Log.e("DEVICE FROM SCAN LIST","$results")
                    listOfdevices = results.toMutableList()

                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("[BLE]", "scan failed with errorcode: $errorCode")
            }
        }
        val bluetoothManager: BluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val scanSettings = ScanSettings.Builder()
            //.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            //.setLegacy(false)
            //.setReportDelay(2000)
            .build()
        scanner = bluetoothManager.adapter.bluetoothLeScanner
        scanner?.startScan(null,scanSettings,bleCallback)

        findViewById<Button>(R.id.disconnectButton).setOnClickListener {
            scanner?.stopScan(bleCallback)
            for(device in listOfdevices){
                Log.e("Device name:","${device.device.name }, ${device.device.uuids}, ${device.device.type}")
            }


        }



    }



private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("Access Fine Location"," permission not granted yet!")
            Log.e("Whitout this permission"," Blutooth devices cannot be searched!")
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                42
            )
        }
    }

    private fun checkBLESupport() {
        // Check if BLE is supported on the device.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
            val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBTIntent, 1)
        }
    }

    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("[BLE]\tSearching", "for BlueCArd...")
        }
    }
    /*

    fun connectToDevice(address: String) {
        device = devices[address]
        scanner!!.stopScan(bleCallback)
        Log.e("[BLE]", "connect to device " + device!!.address)
        bluetoothGatt = device!!.connectGatt(null, false, bleConnectCallback)
    }

    private val bleConnectCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e("[BLE]", "start service discovery " + bluetoothGatt!!.discoverServices())
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                btGattChar = null
                Log.e("[BLE]", "DISCONNECTED with status $status")
                fireDisconnected()
            } else {
                Log.e("[BLE]", "unknown state $newState and status $status")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (null == btGattChar) {
                for (service in gatt.services) {
                    if (service.uuid.toString().toUpperCase().startsWith("4638")) {
                        val gattCharacteristics = service.characteristics
                        for (bgc in gattCharacteristics) {
                            if (bgc.uuid.toString().toUpperCase().startsWith("4638")) {
                                val chprop = bgc.properties
                                if (chprop and BluetoothGattCharacteristic.PROPERTY_WRITE or (chprop and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                                    btGattChar = bgc
                                    Log.e("[BLE]", "CONNECTED and ready to send")
                                    fireConnected()
                                }else{
                                    Log.e("DOES NOT COMPLY WITH CONECTION","NOT")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

     */




}