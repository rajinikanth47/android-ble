package com.mdaq.bluetoothle.manager.server

import android.bluetooth.*
import android.os.Handler
import com.mdaq.bluetoothle.listener.BleLogger
import com.mdaq.bluetoothle.utils.BleUUIDs
import com.mdaq.bluetoothle.utils.BluetoothUtils
import com.mdaq.bluetoothle.utils.ByteUtils
import com.mdaq.bluetoothle.utils.StringUtils

/**
 * Created by rajin on 21/2/18.
 */
class GattServerCallback(bleServer: BleServer) : BluetoothGattServerCallback() {

    private var bleServer:BleServer? =null
    private var bleLogger: BleLogger? =null
    private var mDevices: MutableList<BluetoothDevice>? = null
    private var mHandler: Handler? = null

    init {
        this.bleServer = bleServer
        this.bleLogger = bleServer.getLoggerInst()

        mDevices = mutableListOf()
        mHandler = Handler()
    }

    private fun addDevice(device: BluetoothDevice) {
        bleLogger!!.infoLog("Device added: " + device.address)
        mHandler!!.post { mDevices!!.add(device) }
    }

    private fun removeDevice(device: BluetoothDevice) {
        bleLogger!!.infoLog("Device removed: " + device.address)
        mHandler!!.post { mDevices!!.remove(device) }
    }

    private fun sendAckMessage(message: ByteArray) {
        mHandler!!.post {
            // Reverse message to differentiate original message & response
            val response = ByteUtils.reverse(message)
           // bleLogger!!.infoLog("Sending: " + StringUtils.byteArrayInHexFormat(response))
            notifyCharacteristicEcho(response)
        }
    }
    private fun notifyCharacteristicEcho(value: ByteArray) {
        bleServer!!.notifyCharacteristic(value, BleUUIDs.getCharacteristicEchoUUID(),mDevices!!)
    }

    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
        super.onConnectionStateChange(device, status, newState)
//        bleLogger!!.infoLog("onConnectionStateChange " + device!!.address + "\nstatus " + status + "\nnewState " + newState)
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            addDevice(device!!)
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            removeDevice(device!!)
        }
    }

    override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
//        bleServer!!.onCharacteristicReadRequest(device,requestId,characteristic)

//        bleLogger!!.infoLog("onCharacteristicReadRequest " + characteristic!!.uuid.toString())

        if (BluetoothUtils.requiresResponse(characteristic!!)) {
            // Unknown read characteristic requiring response, send failure
            sendResponse(device!!, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
        }

    }

    private fun sendResponse(device: BluetoothDevice, requestId: Int, status: Int, offset: Int, value: ByteArray?) {
//        mHandler!!.post { bleGattServer!!.sendResponse(device, requestId, status, 0, null) }

        bleServer!!.sendResponse(device,requestId,status)
    }


    override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
        //bleServer!!.onCharacteristicWriteRequest(device,requestId,characteristic)
        //bleLogger!!.infoLog("\nReceived: " + StringUtils.stringFromBytes(value))
        bleServer!!.sendTransferAmounts(value)
        if (characteristic!!.uuid == BleUUIDs.getCharacteristicEchoUUID()) {
            sendResponse(device!!, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
            sendAckMessage(value!!)

        }
    }



    override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
        super.onNotificationSent(device, status)
        bleServer!!.onNotificationSent()
    }
}