package com.statussaver.dele.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.RelativeLayout
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.android.material.snackbar.Snackbar
import com.statussaver.dele.model.StatusModel
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.IOException
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Common {
    val STATUS_DIRECTORY = File(
        Environment.getExternalStorageDirectory().toString() +
                File.separator + "WhatsApp/Media/.Statuses"
    )
    var APP_DIR: String? = null
    fun copyFile(status: StatusModel, context: Context, container: RelativeLayout): Boolean {

        /*val fileName: String
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val currentDateTime = sdf.format(Date())*/

        /*if (Utils.getBack(
                status.filepath,
                "((\\.mp4|\\.webm|\\.ogg|\\.mpK|\\.avi|\\.mkv|\\.flv|\\.mpg|\\.wmv|\\.vob|\\.ogv|\\.mov|\\.qt|\\.rm|\\.rmvb\\.|\\.asf|\\.m4p|\\.m4v|\\.mp2|\\.mpeg|\\.mpe|\\.mpv|\\.m2v|\\.3gp|\\.f4p|\\.f4a|\\.f4b|\\.f4v)$)"
            )!!.isNotEmpty()
        ) {
            fileName = "VID_$currentDateTime.mp4"
        } else {
            fileName = "IMG_$currentDateTime.jpg"
        }*/

        val destFile = File(status.toString() + File.separator + status.filepath)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues()
                val destinationUri: Uri?
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, status.filepath)
                values.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM + "/Dele Statuses"
                )

                val collectionUri: Uri
                if (Utils.getBack(
                        status.filepath,
                        "((\\.mp4|\\.webm|\\.ogg|\\.mpK|\\.avi|\\.mkv|\\.flv|\\.mpg|\\.wmv|\\.vob|\\.ogv|\\.mov|\\.qt|\\.rm|\\.rmvb\\.|\\.asf|\\.m4p|\\.m4v|\\.mp2|\\.mpeg|\\.mpe|\\.mpv|\\.m2v|\\.3gp|\\.f4p|\\.f4a|\\.f4b|\\.f4v)$)"
                    )!!.isNotEmpty()
                ) {
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "video/*")
                    collectionUri = MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    )
                } else {
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "image/*")
                    collectionUri = MediaStore.Images.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    )
                }
                destinationUri = context.contentResolver.insert(collectionUri, values)
                val inputStream =
                    context.contentResolver.openInputStream(status.filepath!!.toUri())
                val outputStream = context.contentResolver.openOutputStream(
                    (destinationUri)!!
                )
                IOUtils.copy(inputStream, outputStream)
            } else {
                val fileP = File(status.filepath.toString())
                FileUtils.copyFile(fileP, destFile)
                destFile.setLastModified(System.currentTimeMillis())
                SingleMediaScanner(context, fileP)
                val data = FileProvider.getUriForFile(
                    context, "com.statussaver.dele.provider",
                    File(destFile.absolutePath)
                )
            }
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    private fun isVideo(path: String?): Boolean {
        val mimeType = URLConnection.guessContentTypeFromName(path)
        return mimeType != null && mimeType.startsWith("video")
    }
}
