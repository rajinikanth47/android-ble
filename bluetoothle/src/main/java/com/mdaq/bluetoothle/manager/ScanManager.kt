package com.mdaq.bluetoothle.manager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.os.ParcelUuid
import com.mdaq.bluetoothle.utils.temp.BluetoothUtilsTemp


/**
 * Created by rajin on 2/2/18.
 */

class ScanManager {


    companion object {
        private var bluetoothLeScanner: BluetoothLeScanner? = null
        private var mScanning: Boolean = false

        fun startScanning(enable: Boolean, bluetoothAdapter: BluetoothAdapter, scanCallback: ScanCallback) {
            bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
            val mHandler = Handler()
            when {
                enable -> {
                    var scanFilters = mutableListOf<ScanFilter>()
                    var settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000).build()
                    BluetoothUtilsTemp.addGATTAttributes()
                    val scanFilter = ScanFilter.Builder()
                            .setServiceUuid(ParcelUuid
                                    .fromString(BluetoothUtilsTemp.TRANSFER_SERVICE_UUID)).build()

                    scanFilters.add(scanFilter)

//                    mHandler.postDelayed({
//                        mScanning = false
//                        bluetoothLeScanner!!.stopScan(scanCallback)
//                    }, BluetoothUtilsTemp.SCAN_PERIOD)

                    mScanning = true
                    bluetoothLeScanner!!.startScan(scanFilters, settings, scanCallback)
                }
                else -> {
                    mScanning = false
                    bluetoothLeScanner!!.stopScan(scanCallback)
                }
            }
        }
    }
}
