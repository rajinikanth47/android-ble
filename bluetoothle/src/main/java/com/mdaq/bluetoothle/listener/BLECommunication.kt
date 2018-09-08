package com.mdaq.bluetoothle.listener

import android.bluetooth.le.ScanResult

/**
 * Created by rajin on 1/2/18.
 */
interface BLECommunication {

   fun onBleSupported()
   fun onBleNotSupported()
   fun onBluetoothEnabled(enable: Boolean, message: String)
   fun onScanResults(callbackType: Int, results: Any?)
   fun onBatchScanResults(results: List<ScanResult>)

}