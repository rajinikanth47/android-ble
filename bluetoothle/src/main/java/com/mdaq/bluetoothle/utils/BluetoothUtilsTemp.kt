package com.mdaq.bluetoothle.utils.temp

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/**
 * Created by rajin on 1/2/18.
 */

object BluetoothUtilsTemp {

     var BLUETOOTH_LE_LIBRARY_VERSION = "V.0.1"
     var BLUETOOTH_ENABLE_REQUEST:Int = 123

    val UUID_APP_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb"
    val UUID_BATTERY_LEVEL_UUID = "00002a19-0000-1000-8000-00805f9b34fb"

    val TRANSFER_SERVICE_UUID = "E20A39F4-73F5-4BC4-A12F-17D1AD666666"


    val TRANSFER_CHARACTERISTIC_UUID = "08590F7E-DB05-467E-8757-72F6F66666D7"
    val NOTIFY_CHARACTERISTIC_UUID =   "08590F7A-DB05-467E-8757-72F6F66666D7"
    val NOTIFY_MTU = 20

    val userIdentifire : String = "C39SKZLQHFY7"
    val commonPOSIdentifire : String = "mdaqPOS"






    val REQUEST_BLUETOOTH_ENABLE_CODE = 101
    val REQUEST_LOCATION_ENABLE_CODE = 102

    val SCAN_PERIOD:Long = 10000

    private var attributes = HashMap<String,String>()



     fun isBleSupported(context: Context): Boolean{
          return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
     }

    fun requestEnableBluetooth(activity: Activity) {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(intent, BLUETOOTH_ENABLE_REQUEST)
    }

    fun addGATTAttributes(){
        attributes.put(UUID_BATTERY_LEVEL_UUID, "Battery Level")
        attributes.put(UUID_APP_SERVICE, "Battery Service")
    }

    fun lookup(uuid: String): String {
        return attributes!![uuid]!!
    }


}