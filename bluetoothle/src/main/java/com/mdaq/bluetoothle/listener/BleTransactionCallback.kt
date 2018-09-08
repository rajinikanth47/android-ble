package com.mdaq.bluetoothle.listener

/**
 * Created by rajin on 22/2/18.
 */
interface BleTransactionCallback{
    fun transactionResponse(msg:Any?)
    fun errorCallBack(msg:Any?)
}