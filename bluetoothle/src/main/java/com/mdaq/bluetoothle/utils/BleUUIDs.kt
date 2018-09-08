package com.mdaq.bluetoothle.utils

import android.bluetooth.BluetoothGattCharacteristic
import com.mdaq.bluetoothle.utils.temp.BluetoothUtilsTemp.SCAN_PERIOD
import java.util.*

/**
 * Created by rajin on 21/2/18.
 */
//data class BleUUIDs(val service_uuid: UUID, val characteristic_uuid: UUID)


object BleUUIDs {


    var SERVICE_STRING = ""
    var CHARACTERISTIC_ECHO_STRING = ""
    var  NOTIFY_CHARACTERISTIC_UUID = ""
    var SCAN_PERIOD: Long = 5000


    fun setUUIDKeys(serviceUUID: String,characteristicUUID: String,notifyUUID: String,period: Long){
        SERVICE_STRING = serviceUUID
        CHARACTERISTIC_ECHO_STRING = characteristicUUID
        NOTIFY_CHARACTERISTIC_UUID = notifyUUID
        SCAN_PERIOD = period
    }


    fun getServiceUUID(): UUID {
         return UUID.fromString(SERVICE_STRING)
    }

    fun getCharacteristicEchoUUID() :UUID {
        return UUID.fromString(CHARACTERISTIC_ECHO_STRING)
    }

    fun getNotifyCharacteristicsUUID(): UUID{
        return UUID.fromString(NOTIFY_CHARACTERISTIC_UUID)
    }



}