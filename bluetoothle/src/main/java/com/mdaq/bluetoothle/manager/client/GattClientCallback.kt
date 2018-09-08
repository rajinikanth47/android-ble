package com.mdaq.bluetoothle.manager.client

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import com.mdaq.bluetoothle.listener.BleLogger
import com.mdaq.bluetoothle.utils.BluetoothUtils


/**
 * Created by rajin on 21/2/18.
 */
class GattClientCallback(bleClient: BleClient): BluetoothGattCallback() {

    private var bleClient: BleClient? =null
    private var bleLogger: BleLogger? =null

    init {
        this.bleClient = bleClient
        this.bleLogger = bleClient.getLoggerInst()
    }
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        bleLogger!!.infoLog("onConnectionStateChange newState: " + newState)

        if (status == BluetoothGatt.GATT_FAILURE) {
            bleLogger!!.errorLog("Connection Gatt failure status " + status)
            bleClient!!.disconnectGattServer()
            return
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            // handle anything not SUCCESS as failure
            bleLogger!!.errorLog("Connection not GATT sucess status " + status)
            bleClient!!.disconnectGattServer()
            return
        }

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            bleLogger!!.infoLog("Connected to device " + gatt.device.address)
            bleClient!!.setConnected(true)
            gatt.discoverServices()
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            bleLogger!!.infoLog("Disconnected from device")
            bleClient!!.disconnectGattServer()
        }
    }


    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if(status != BluetoothGatt.GATT_SUCCESS){
            bleLogger!!.infoLog("Device service discovery unsuccessful, status " + status)
            return
        }

        val matchingCharacteristics = BluetoothUtils.findCharacteristics(gatt!!)
        if (matchingCharacteristics.isEmpty()) {
            bleLogger!!.errorLog("Unable to find characteristics.")
            return
        }
        bleLogger!!.infoLog("Initializing: setting write type and enabling notification");

        for (characteristic in matchingCharacteristics) {
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            bleClient!!.enableCharacteristicNotification(gatt!!, characteristic)
        }
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            bleLogger!!.infoLog("Characteristic written successfully")
        } else {
            bleLogger!!.errorLog("Characteristic write unsuccessful, status: " + status);
            bleClient!!.disconnectGattServer()
        }
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        super.onCharacteristicRead(gatt, characteristic, status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            bleLogger!!.infoLog("Characteristic read successfully");
            bleClient!!.readCharacteristic(characteristic!!)
        } else {
            bleLogger!!.infoLog("Characteristic read unsuccessful, status: " + status);
            // Trying to read from the Time Characteristic? It doesnt have the property or permissions
            // set to allow this. Normally this would be an error and you would want to:
            // disconnectGattServer();
        }
    }

}