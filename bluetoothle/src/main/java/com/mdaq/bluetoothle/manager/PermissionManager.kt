package com.mdaq.bluetoothle.manager

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log


/**
 * Created by rajin on 12/2/18.
 */
class PermissionManager{

    companion object {
        private val TAG = PermissionManager::class.java!!.name
        val PERMISSION_ALL = 1

        @Throws(PackageManager.NameNotFoundException::class)
        private fun getPermissions(context: Context): Array<String> {
            val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            return info.requestedPermissions
        }

        private fun doesAppNeedPermissions(): Boolean {
            return android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
        }

        fun askPermissions(activity: Activity) {
            if (doesAppNeedPermissions()) {
                try {
                    var permissions = getPermissions(activity)
                    if (!checkPermissions(activity, *permissions)) {
                        ActivityCompat.requestPermissions(activity, permissions,
                                PERMISSION_ALL)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }else{
                Log.d(this.TAG,"User approval not required in this device for  Aoo external permissions.")
            }
        }

       private fun checkPermissions(context: Context?, vararg permissions: String): Boolean {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null &&
                    permissions != null) {
                permissions
                        .filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
                        .forEach { return false }
            }
            return true
        }
    }

}