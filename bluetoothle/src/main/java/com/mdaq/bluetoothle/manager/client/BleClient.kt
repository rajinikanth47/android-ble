package com.mdaq.bluetoothle.manager.client

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.ParcelUuid
import com.mdaq.bluetoothle.listener.BleLogger
import com.mdaq.bluetoothle.listener.ScanCompletedListener
import com.mdaq.bluetoothle.utils.BleUUIDs
import com.mdaq.bluetoothle.utils.BluetoothUtils
import com.mdaq.bluetoothle.utils.StringUtils
import com.mdaq.bluetoothle.utils.temp.BluetoothUtilsTemp.SCAN_PERIOD
import java.util.*


/**
 * Created by rajin on 21/2/18.
 */
class BleClient(activity: Activity, bleLogger: BleLogger,scanListener: ScanCompletedListener) {

    private var activity: Activity? =null
    private var mBluetoothManager: BluetoothManager? =null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBleLogger: BleLogger? =null
    private var mGatt: BluetoothGatt? = null
    private var mScanCompletedListener: ScanCompletedListener? =null

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_FINE_LOCATION = 2
    private var mScanning: Boolean = false
    private var mConnected: Boolean = false
    private var mHandler: Handler? = null

    private var mScanResults: MutableMap<String, BluetoothDevice>? = null
    private var mScanCallback: ScanCallback? = null
    private var mBluetoothLeScanner: BluetoothLeScanner? = null
    private var mEchoInitialized: Boolean = false

    init {
        this.activity = activity
        this.mBluetoothManager = this.activity!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        this.mBluetoothAdapter = mBluetoothManager!!.adapter
        this.mBleLogger = bleLogger
        mScanCompletedListener = scanListener
    }

    fun getDeviceInfo(): String {
        return "Device Info" + "\nName: " + mBluetoothAdapter!!.name + "\nAddress: " + mBluetoothAdapter!!.address
    }

    fun getDeviceName():String{
        return mBluetoothAdapter!!.name
    }

    // Check if bluetooth is enabled
    fun enableBluetooth(){
        if (mBluetoothAdapter == null || !mBluetoothAdapter!!.isEnabled) {
            // Request user to enable it
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity!!.startActivity(enableBtIntent)
            activity!!.finish()
            return
        }
    }

    fun checkBleSupport(){
        // Check low energy support
        if (!activity!!.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // Get a newer device
            mBleLogger!!.infoLog("No LE Support.")
            activity!!.finish()
            return
        }
    }

    private fun requestBluetoothEnable() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity!!.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        mBleLogger!!.infoLog("Requested user enables Bluetooth. Try starting the scan again.")
    }

    private fun hasLocationPermissions(): Boolean {
        return activity!!.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        activity!!.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FINE_LOCATION)
        mBleLogger!!.infoLog("Requested user enable Location. Try starting the scan again.")
    }



    private fun hasPermissions(): Boolean {
        if (mBluetoothAdapter == null || !mBluetoothAdapter!!.isEnabled) {
            requestBluetoothEnable()
            return false
        } else if (!hasLocationPermissions()) {
            requestLocationPermission()
            return false
        }
        return true
    }

    fun disconnectGattServer() {
        mBleLogger!!.infoLog("Closing Gatt connection")

        mConnected = false
        if (mGatt != null) {
            mGatt!!.disconnect()
            mGatt!!.close()
        }

        mScanCompletedListener!!.onDeviceDisconnected()
    }


    fun sendMessage(message: String){

        if (!mConnected || !mEchoInitialized) {
            return
        }

        val characteristic = BluetoothUtils.findEchoCharacteristic(mGatt!!)
        if (characteristic == null) {
            mBleLogger!!.errorLog("Unable to find echo characteristic.")
            disconnectGattServer()
            mScanCompletedListener!!.onMessageError()
            return
        }

        val messageBytes = StringUtils.bytesFromString(message)
        if (messageBytes.isEmpty()) {
            mBleLogger!!.errorLog("Unable to convert message to bytes")
            mScanCompletedListener!!.onMessageError()
            return
        }

        characteristic.value = messageBytes

        val success = mGatt!!.writeCharacteristic(characteristic)
        if (success) {
            mBleLogger!!.infoLog("Wrote: " + StringUtils.byteArrayInHexFormat(messageBytes))
            mScanCompletedListener!!.onMessageSuccess()
        } else {
            mBleLogger!!.errorLog("Failed to write data")
            mScanCompletedListener!!.onMessageError()
        }

    }



    fun startScan(){
        if (!hasPermissions() || mScanning) {
            return
        }

        disconnectGattServer()

        mScanResults = mutableMapOf()
        mScanCallback = BtleScanCallback(mScanResults!!)

        mBluetoothLeScanner = mBluetoothAdapter!!.bluetoothLeScanner

        // Note: Filtering does not work the same (or at all) on most devices. It also is unable to
        // search for a mask or anything less than a full UUID.
        // Unless the full UUID of the server is known, manual filtering may be necessary.
        // For example, when looking for a brand of device that contains a char sequence in the UUID
        val scanFilter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(BleUUIDs.getServiceUUID()))
                .build()
        val filters = ArrayList<ScanFilter>()
        filters.add(scanFilter)

        val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()

        mBluetoothLeScanner!!.startScan(filters, settings, mScanCallback)

        mHandler = Handler()
        mHandler!!.postDelayed({ this.stopScan() }, SCAN_PERIOD)

        mScanning = true
        mBleLogger!!.infoLog("Started scanning.")

    }


    private fun stopScan() {
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter!!.isEnabled && mBluetoothLeScanner != null) {
            mBluetoothLeScanner!!.stopScan(mScanCallback)
            scanComplete()
        }

        mScanCallback = null
        mScanning = false
        mHandler = null
        mBleLogger!!.infoLog("Stopped scanning.")
        mScanCompletedListener!!.onScanCompleted(null)
    }

    private fun scanComplete() {
        if (mScanResults!!.isEmpty()) {
            return
        }
        mScanCompletedListener!!.onScanCompleted(mScanResults)
    }


    fun connectDevice(device: BluetoothDevice) {
        mBleLogger!!.infoLog("Connecting to " + device.address)
        val gattClientCallback = GattClientCallback(this)
        mGatt = device.connectGatt(activity, false, gattClientCallback,BluetoothDevice.TRANSPORT_LE)
        val notifyCharacteristic = BluetoothGattCharacteristic(BleUUIDs.getNotifyCharacteristicsUUID(),BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE)
        mGatt!!.setCharacteristicNotification(notifyCharacteristic,true)
    }

    fun getLoggerInst(): BleLogger{
        return mBleLogger!!
    }

    fun setConnected(connected: Boolean) {
        mConnected = connected
    }


     fun enableCharacteristicNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val characteristicWriteSuccess = gatt.setCharacteristicNotification(characteristic, true)
        if (characteristicWriteSuccess) {
            mBleLogger!!.infoLog("Characteristic notification set successfully for " + characteristic.uuid.toString())

            if (BluetoothUtils.isEchoCharacteristic(characteristic)) {
                initializeEcho()
            }
            mScanCompletedListener!!.onDeviceConnected()

        } else {
            mBleLogger!!.errorLog("Characteristic notification set failure for " + characteristic.uuid.toString())
            mScanCompletedListener!!.onDeviceDisconnected()
        }
    }

     fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        val messageBytes = characteristic.value
         mBleLogger!!.infoLog("Read: " + StringUtils.byteArrayInHexFormat(messageBytes))
        val message = StringUtils.stringFromBytes(messageBytes)
        if (message == null) {
            mBleLogger!!.errorLog("Unable to convert bytes to string")
            return
        }
         mBleLogger!!.infoLog("Received message: " + message!!)
    }

    private fun initializeEcho() {
        mEchoInitialized = true
    }


    // Callbacks

    private inner class BtleScanCallback internal constructor(private val mScanResults: MutableMap<String, BluetoothDevice>) : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            addScanResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                addScanResult(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            mBleLogger!!.errorLog("BLE Scan Failed with code $errorCode")
        }

        private fun addScanResult(result: ScanResult) {
            val device = result.device
            val deviceAddress = device.address
            mScanResults[deviceAddress] = device
        }
    }



}