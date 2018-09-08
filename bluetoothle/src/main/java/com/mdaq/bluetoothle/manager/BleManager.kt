package com.mdaq.bluetoothle.manager

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.mdaq.bluetoothle.listener.BLECommunication
import com.mdaq.bluetoothle.utils.temp.BluetoothUtilsTemp


/**
 * Created by rajin on 1/2/18.
 */

class BleManager(context:Context,bleListener: BLECommunication) : ScanCallback() {

    private val TAG = BleManager::class.java.name

    private var context: Context?= null
    private var bleCommunicationListener: BLECommunication?= null
    private var bluetoothManager: BluetoothManager?= null
    private var bluetoothAdapter: BluetoothAdapter?= null

    init {
        this.context = context
        this.bleCommunicationListener = bleListener
        validateBleFeature()
    }

    private fun validateBleFeature() = when {
        BluetoothUtilsTemp.isBleSupported(this.context!!) -> {
            this.bleCommunicationListener!!.onBleSupported()
            init()
        }
        else -> this.bleCommunicationListener!!.onBleNotSupported()
    }

    private fun init() {
        bluetoothManager = this.context!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager!!.adapter
    }

    fun enableBluetooth(activity: Activity) = when {
        bluetoothAdapter != null && !bluetoothAdapter!!.isEnabled -> BluetoothUtilsTemp.requestEnableBluetooth(activity)
        else -> bleCommunicationListener!!.onBluetoothEnabled(true, "Bluetooth enabled")
    }

    fun onRequestPermissionsBluetooth(requestCode: Int, resultCode: Int, data: Intent?) =
            if(requestCode == Activity.RESULT_OK && resultCode == BluetoothUtilsTemp.BLUETOOTH_ENABLE_REQUEST){
                bleCommunicationListener!!.onBluetoothEnabled(true,"Bluetooth enabled")
            }else{
                bleCommunicationListener!!.onBluetoothEnabled(false,"Bluetooth doesn't  enable")
            }

    fun startScan(){
            ScanManager.startScanning(true,this.bluetoothAdapter!!,this)
    }

    fun stopScan(){
        ScanManager.startScanning(false,this.bluetoothAdapter!!,this)
    }

    fun connectingGATTServer(device: BluetoothDevice) {
        if(this.bluetoothAdapter != null){

            val device = this.bluetoothAdapter!!.getRemoteDevice(device.address)

            if (device == null) {
                Log.w(TAG, "Device not found")
            }

            val handler = Handler(Looper.getMainLooper())

            handler.post {

                val connectGatt = device?.connectGatt(context, false, BleGattCallback())
                connectGatt!!.connect()
            }
        }
    }


    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)
        this.bleCommunicationListener!!.onScanResults(callbackType,result)
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        super.onBatchScanResults(results)
        this.bleCommunicationListener!!.onBatchScanResults(results!!)
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        Log.d(TAG,"Scanning Filed!!")
    }






}

