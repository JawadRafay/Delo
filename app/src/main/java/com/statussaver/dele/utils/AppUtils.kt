package com.statussaver.dele.utils

import android.content.Context
import android.content.pm.PackageManager

class AppUtils(val context: Context) {

    fun isPackageInstalled(packageName: String?): Boolean {
        return try {
            //Just check if app's icon is present
            packageName?.let { context.packageManager.getApplicationIcon(it) }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }
}