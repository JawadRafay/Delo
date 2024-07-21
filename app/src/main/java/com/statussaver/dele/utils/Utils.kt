package com.statussaver.dele.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import android.os.Environment

import androidx.core.content.ContextCompat
import com.statussaver.dele.R
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URLConnection
import java.util.*
import java.util.regex.Pattern

class Utils {

    companion object{
        var mPath: String? = null
        var perRequest = 21
        var date: Date? = null
        var dateCompareOne: Date? = null

        var permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )


        fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
            if (permissions != null) {
                for (permission in permissions) {
                    if (ContextCompat.checkSelfPermission(
                            context!!,
                            permission!!
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return true
                    }
                }
            }
            return false
        }

        fun mediaScanner(
            context: Context?,
            newFilePath: String,
            oldFilePath: String?,
            fileType: String
        ) {
            try {
                MediaScannerConnection.scanFile(context,
                    arrayOf(newFilePath + File(oldFilePath.toString()).name),
                    arrayOf(fileType),
                    object : MediaScannerConnectionClient {
                        override fun onMediaScannerConnected() {}
                        override fun onScanCompleted(path: String, uri: Uri) {}
                    })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun getBack(paramString1: String?, paramString2: String?): String? {
            val localMatcher = Pattern.compile(paramString2.toString()).matcher(paramString1.toString())
            return if (localMatcher.find()) {
                localMatcher.group(1)
            } else ""
        }

        fun download(context: Context, path: String?): Boolean {
            return copyFileInSavedDir(context, path)
        }

        fun isImageFile(path: String?): Boolean {
            val mimeType = URLConnection.guessContentTypeFromName(path)
            return mimeType != null && mimeType.startsWith("image")
        }

        fun isVideoFile(path: String?): Boolean {
            val mimeType = URLConnection.guessContentTypeFromName(path)
            return mimeType != null && mimeType.startsWith("video")
        }


        fun copyFileInSavedDir(context: Context, file: String?): Boolean {
            return try {
                if (isImageFile(file)) {
                    FileUtils.copyFileToDirectory(File(file.toString()), getDir(context, "Images"))
                    mediaScanner(context, getDir(context, "Images").toString() + "/", file, "image/*")
                } else {
                    FileUtils.copyFileToDirectory(File(file.toString()), getDir(context, "Videos"))
                    mediaScanner(context, getDir(context, "Videos").toString() + "/", file, "video/*")
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        fun getDir(context: Context, folder: String): File {
            val rootFile = File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + "Download" + File.separator + context.resources.getString(
                    R.string.app_name
                ) + File.separator + folder
            )
            rootFile.mkdirs()
            return rootFile
        }

        fun compareDates(startDate: Long?, fileDate: Long): Boolean {
            return fileDate> startDate!!
        }

        fun getDate(): Long {
            val calendar = Calendar.getInstance()
            return calendar.timeInMillis
        }
    }
}