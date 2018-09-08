package com.mdaq.bluetoothle.listener

import android.bluetooth.BluetoothDevice

/**
 * Created by rajin on 21/2/18.
 */
interface ScanCompletedListener{
    fun onScanCompleted(resultList: MutableMap<String, BluetoothDevice>?)
    fun onDeviceConnected()
    fun onDeviceDisconnected()
    fun onMessageSuccess()
    fun onMessageError()
}