package com.mdaq.bluetoothle.utils

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService


import java.util.ArrayList


/**
 * Created by rajin on 20/2/18.
 */

object BluetoothUtils {
    // Characteristics

    private var CHARACTERISTIC_ECHO_STRING = BleUUIDs.CHARACTERISTIC_ECHO_STRING
    private var  SERVICE_STRING = BleUUIDs.SERVICE_STRING


    fun findCharacteristics(bluetoothGatt: BluetoothGatt): List<BluetoothGattCharacteristic> {
        val matchingCharacteristics = ArrayList<BluetoothGattCharacteristic>()

        val serviceList = bluetoothGatt.services
        val service = findService(serviceList) ?: return matchingCharacteristics

        val characteristicList = service.characteristics
        for (characteristic in characteristicList) {
            if (isMatchingCharacteristic(characteristic)) {
                matchingCharacteristics.add(characteristic)
            }
        }

        return matchingCharacteristics
    }

    fun findEchoCharacteristic(bluetoothGatt: BluetoothGatt): BluetoothGattCharacteristic? {
        return findCharacteristic(bluetoothGatt, CHARACTERISTIC_ECHO_STRING)
    }

    private fun findCharacteristic(bluetoothGatt: BluetoothGatt, uuidString: String): BluetoothGattCharacteristic? {
        val serviceList = bluetoothGatt.services
        val service = findService(serviceList) ?: return null

        val characteristicList = service.characteristics
        for (characteristic in characteristicList) {
            if (characteristicMatches(characteristic, uuidString)) {
                return characteristic
            }
        }

        return null
    }

    fun isEchoCharacteristic(characteristic: BluetoothGattCharacteristic): Boolean {
        return characteristicMatches(characteristic, CHARACTERISTIC_ECHO_STRING)
    }

    private fun characteristicMatches(characteristic: BluetoothGattCharacteristic?, uuidString: String): Boolean {
        if (characteristic == null) {
            return false
        }
        val uuid = characteristic.uuid
        return uuidMatches(uuid.toString(), uuidString)
    }

    private fun isMatchingCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {
        if (characteristic == null) {
            return false
        }
        val uuid = characteristic.uuid
        return matchesCharacteristicUuidString(uuid.toString())
    }

    private fun matchesCharacteristicUuidString(characteristicIdString: String): Boolean {
        return uuidMatches(characteristicIdString, CHARACTERISTIC_ECHO_STRING)
    }

    fun requiresResponse(characteristic: BluetoothGattCharacteristic): Boolean {
        return characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
    }

    fun requiresConfirmation(characteristic: BluetoothGattCharacteristic): Boolean {
        return characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE == BluetoothGattCharacteristic.PROPERTY_INDICATE
    }

    // Service

    private fun matchesServiceUuidString(serviceIdString: String): Boolean {
        return uuidMatches(serviceIdString, SERVICE_STRING)
    }

    private fun findService(serviceList: List<BluetoothGattService>): BluetoothGattService? {
        for (service in serviceList) {
            val serviceIdString = service.uuid
                    .toString()
            if (matchesServiceUuidString(serviceIdString)) {
                return service
            }
        }
        return null
    }

    // String matching

    // If manually filtering, substring to match:
    // 0000XXXX-0000-0000-0000-000000000000
    private fun uuidMatches(uuidString: String, vararg matches: String): Boolean {
        return matches.any { uuidString.equals(it, ignoreCase = true) }
    }
}