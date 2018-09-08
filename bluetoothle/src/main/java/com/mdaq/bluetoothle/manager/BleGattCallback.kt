package com.mdaq.bluetoothle.manager

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED


/**
 * Created by rajin on 14/2/18.
 */
class BleGattCallback : BluetoothGattCallback(){

    private var TAG = BleGattCallback::class.java.name

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        Log.d(TAG, "onConnectionStateChange " + newState)



        val intentAction: String

        if (newState === BluetoothProfile.STATE_CONNECTED) {
            Log.i(TAG, "Connected to GATT server.")
            // Attempts to discover services after successful connection.
            Log.i(TAG, "Attempting to start service discovery:" + gatt!!.discoverServices())

        } else if (newState === STATE_DISCONNECTED) {

            Log.i(TAG, "Disconnected from GATT server.")

        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        Log.d(TAG,"onServicesDiscovered"+status)
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        super.onCharacteristicRead(gatt, characteristic, status)
        gatt!!.setCharacteristicNotification(characteristic,true)
        Log.d(TAG,"onCharacteristicRead"+status)
    }

}