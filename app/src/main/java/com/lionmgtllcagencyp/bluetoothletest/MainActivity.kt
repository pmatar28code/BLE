package com.lionmgtllcagencyp.bluetoothletest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity(), BLEControllerListener {
    private var logView: TextView? = null
    private var connectButton: Button? = null
    private var disconnectButton: Button? = null
    private var switchLEDButton: Button? = null
    private var bleController: BLEController? = null
    private var remoteControl: RemoteControl? = null
    private var deviceAddress: String? = null
    private var isLEDOn = false
    private var isAlive = false
    private var heartBeatThread: Thread? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bleController = BLEController.getInstance(this)
        remoteControl = bleController?.let { RemoteControl(it) }
        logView = findViewById(R.id.logView)
        logView?.movementMethod = ScrollingMovementMethod()
        initConnectButton()
        initDisconnectButton()
        initSwitchLEDButton()
        checkBLESupport()
        checkPermissions()
        disableButtons()
    }

    fun startHeartBeat() {
        isAlive = true
        heartBeatThread = createHeartBeatThread()
        heartBeatThread!!.start()
    }

    fun stopHeartBeat() {
        if (isAlive) {
            isAlive = false
            heartBeatThread!!.interrupt()
        }
    }

    private fun createHeartBeatThread(): Thread {
        return object : Thread() {
            override fun run() {
                while (isAlive) {
                    heartBeat()
                    try {
                        sleep(1000L)
                    } catch (ie: InterruptedException) {
                        return
                    }
                }
            }
        }
    }

    private fun heartBeat() {
        remoteControl?.heartbeat()
    }

    private fun initConnectButton() {
        connectButton = findViewById(R.id.connectButton)
        connectButton?.setOnClickListener(View.OnClickListener {
            connectButton?.setEnabled(false)
            log("Connecting...")
            deviceAddress?.let { it1 -> bleController?.connectToDevice(it1) }
        })
    }

    private fun initDisconnectButton() {
        disconnectButton = findViewById(R.id.disconnectButton)
        disconnectButton?.setOnClickListener(View.OnClickListener {
            disconnectButton?.setEnabled(false)
            log("Disconnecting...")
            bleController?.disconnect()
        })
    }

    private fun initSwitchLEDButton() {
        switchLEDButton = findViewById(R.id.switchButton)
        switchLEDButton?.setOnClickListener(View.OnClickListener {
            isLEDOn = !isLEDOn
            remoteControl?.switchLED(isLEDOn)
            log("LED switched " + if (isLEDOn) "On" else "Off")
        })
    }

    private fun disableButtons() {
        runOnUiThread {
            connectButton!!.isEnabled = false
            disconnectButton!!.isEnabled = false
            switchLEDButton!!.isEnabled = false
        }
    }

    private fun log(text: String) {
        runOnUiThread {
            logView!!.text = """
     ${logView!!.text}
     $text
     """.trimIndent()
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            log("\"Access Fine Location\" permission not granted yet!")
            log("Whitout this permission Blutooth devices cannot be searched!")
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
        deviceAddress = null
        bleController = BLEController.getInstance(this)
        bleController?.addBLEControllerListener(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            log("[BLE]\tSearching for BlueCArd...")
            bleController?.init()
        }
    }

    override fun onPause() {
        super.onPause()
        bleController?.removeBLEControllerListener(this)
        stopHeartBeat()
    }

    override fun BLEControllerConnected() {
        log("[BLE]\tConnected")
        runOnUiThread {
            disconnectButton!!.isEnabled = true
            switchLEDButton!!.isEnabled = true
        }
        startHeartBeat()
    }

    override fun BLEControllerDisconnected() {
        log("[BLE]\tDisconnected")
        disableButtons()
        runOnUiThread { connectButton!!.isEnabled = true }
        isLEDOn = false
        stopHeartBeat()
    }

    override fun BLEDeviceFound(name: String?, address: String?) {
        log("Device $name found with address $address")
        deviceAddress = address
        connectButton!!.isEnabled = true
    }
}