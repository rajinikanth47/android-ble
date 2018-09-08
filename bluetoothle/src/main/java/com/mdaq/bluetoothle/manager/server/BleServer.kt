package com.mdaq.bluetoothle.manager.server

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.ParcelUuid
import com.mdaq.bluetoothle.listener.BleLogger
import com.mdaq.bluetoothle.listener.BleTransactionCallback
import com.mdaq.bluetoothle.utils.BleUUIDs
import com.mdaq.bluetoothle.utils.BluetoothUtils
import com.mdaq.bluetoothle.utils.StringUtils
import java.util.*

/**
 * Created by rajin on 21/2/18.
 */
class BleServer(activity: Activity, bleLogger: BleLogger,bleTransactionCallback: BleTransactionCallback){

    private var activity: Activity? =null
    private var mBluetoothManager: BluetoothManager? =null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBleLogger: BleLogger? =null
    private var mBluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private var mGattServer: BluetoothGattServer? = null
    private var mHandler: Handler? = null
    private var bleTransactionCallback: BleTransactionCallback? =null

    init {
        this.activity = activity
        this.mBluetoothManager = this.activity!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        this.mBluetoothAdapter = mBluetoothManager!!.adapter
        this.mBleLogger = bleLogger
        this.bleTransactionCallback = bleTransactionCallback
        mHandler = Handler()
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
        if (!activity!!.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            mBleLogger!!.infoLog("No LE Support.")
            when {
                bleTransactionCallback!=null -> bleTransactionCallback!!.errorCallBack("Bluetooth Hardware not supported.")
            }
            activity!!.finish()
            return
        }
        if (!mBluetoothAdapter!!.isMultipleAdvertisementSupported) {
            mBleLogger!!.infoLog("No Advertising Support.")
            when {
                bleTransactionCallback!=null -> bleTransactionCallback!!.errorCallBack("Bluetooth Hardware(adv) not supported")
            }
            activity!!.finish()
            return
        }

        mBluetoothLeAdvertiser = mBluetoothAdapter!!.bluetoothLeAdvertiser
    }

    fun getDeviceInfo(): String {
        return "Device Info" + "\nName: " + mBluetoothAdapter!!.name + "\nAddress: " + mBluetoothAdapter!!.address
    }

    fun getLoggerInst(): BleLogger{
        return mBleLogger!!
    }


    fun setUpServer() {
        try {
            mGattServer = mBluetoothManager!!.openGattServer(this.activity, GattServerCallback(this))

            val service = BluetoothGattService(BleUUIDs.getServiceUUID(), BluetoothGattService.SERVICE_TYPE_PRIMARY)

            val writeCharacteristic = BluetoothGattCharacteristic(BleUUIDs.getCharacteristicEchoUUID(), BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE)
            val notifyCharacteristic = BluetoothGattCharacteristic(BleUUIDs.getNotifyCharacteristicsUUID(), BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY)
            service.addCharacteristic(writeCharacteristic)
            service.addCharacteristic(notifyCharacteristic)
            mGattServer!!.addService(service)
        }catch (e:Exception){
            when {
                bleTransactionCallback!=null -> bleTransactionCallback!!.errorCallBack("Bluetooth setup not successful")
            }
            throw IllegalArgumentException("Error")
        }
    }

     fun stopServer(){
        when {
            mGattServer != null -> mGattServer!!.close()
        }
    }

    fun restartServer(){
        stopAdvertising()
        stopServer()
        setUpServer()
        startAdvertising()
    }


    fun startAdvertising(){

        when (mBluetoothLeAdvertiser) {
            null -> {
                when {
                    bleTransactionCallback!=null -> bleTransactionCallback!!.errorCallBack("Fliq advertising failed, cannot be visible to others")
                }
                return
            }
        }

        val settings = AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build()

        val parcelUuid = ParcelUuid(BleUUIDs.getServiceUUID())


        val data = AdvertiseData.Builder().setIncludeDeviceName(true)
                .addServiceUuid(parcelUuid)
                .build()

        mBluetoothLeAdvertiser!!.startAdvertising(settings, data, mAdvertiseCallback)

    }

     fun stopAdvertising(){
        when {
            mBluetoothLeAdvertiser != null -> mBluetoothLeAdvertiser!!.stopAdvertising(mAdvertiseCallback)
        }
    }

    private val mAdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            mBleLogger!!.infoLog("Peripheral advertising started.")
        }

        override fun onStartFailure(errorCode: Int) {
            mBleLogger!!.infoLog("Peripheral advertising failed: $errorCode")
            when {
                bleTransactionCallback!=null -> bleTransactionCallback.errorCallBack("Fliq advertising failed, cannot be visible to others")
            }
        }
    }


    /**
     * Gatt server callbacks
     */

    fun onNotificationSent() {

    }

    fun notifyCharacteristic(value: ByteArray, characterUUID: UUID, mDevices: MutableList<BluetoothDevice>) {

        mHandler!!.post {
            val service = mGattServer!!.getService(BleUUIDs.getServiceUUID())
            val characteristic = service.getCharacteristic(characterUUID)
            characteristic.value = value
            val confirm = BluetoothUtils.requiresConfirmation(characteristic)
            for (device in mDevices!!) {
                mGattServer!!.notifyCharacteristicChanged(device, characteristic, confirm)
            }
        }
    }
    fun sendResponse(device: BluetoothDevice, requestId: Int, status: Int) {
            mGattServer!!.sendResponse(device,requestId,status,0,null)
    }
    fun sendTransferAmounts(value: ByteArray?) {
        var amount = StringUtils.stringFromBytes(value)

        if(amount!!.contains(','))
                 bleTransactionCallback!!.transactionResponse(amount)
    }
}