package com.statussaver.dele.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.UriPermission
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import com.statussaver.dele.database.PrefManager
import com.statussaver.dele.model.AudioModel
import com.statussaver.dele.model.StatusModel
import java.io.File
import java.util.Arrays
import java.util.concurrent.Executors

class DataServiceAboveTenVersion : Service() {
    private val TAG = "DataServiceAboveTenVersion"
    var prefManager: PrefManager? = null

    companion object {
        val whatsAppStatus = MutableLiveData<List<StatusModel>>()
        val whatsAppImages = MutableLiveData<List<StatusModel>>()
        val whatsAppVideos = MutableLiveData<List<StatusModel>>()
        val whatsAppAudio = MutableLiveData<List<AudioModel>>()
        val whatsAppDocument = MutableLiveData<List<AudioModel>>()
    }

    override fun onCreate() {
        super.onCreate()
        prefManager = PrefManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val appContext = applicationContext
        getWhatsappStatus(appContext)

        getWhatsAppImages(appContext)

        getWhatsAppVideos(appContext)

        getWhatsAppDocument(appContext)

        getWhatsAppAudios(appContext)

        /*val context = intent?.getParcelableExtra("context")
        context?.let {getWhatsappStatusAbove10(it)}*/

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun getWhatsappStatus(context: Context) {
        val list = context.contentResolver.persistedUriPermissions
        if (list.isNotEmpty()) {
            val file = DocumentFile.fromTreeUri(context, list[0].uri)
            Log.d("check_tag", "CheckDirectoryFile: " + file)
            val cloneList: MutableList<StatusModel> = java.util.ArrayList()

            if (file != null) {
                if (file.isDirectory) {
                    val listFile = file.listFiles()
                    Arrays.sort(listFile) { file1, file2 ->
                        file1.name!!.compareTo(file2.name!!)
                    }
                    for (i in listFile) {
                        if (!i.name.equals(".nomedia")) {
                            if (i.isFile) {
                                val model = StatusModel()
                                model.filepath = i.uri.toString()

                                cloneList.add(model)
                            }
                        }
                    }
                }
            }
            whatsAppStatus.postValue(cloneList)
        } else {
            // Handle case when the list is empty
            Log.d(TAG, "persistedUriPermissions list is empty")
        }
    }

    private fun getWhatsAppImages(context: Context) {
        val cloneList: MutableList<StatusModel> = mutableListOf()
        // Define the folder path
        val folderPath =
            "/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images"

        val contentResolver = context.contentResolver
        val projection = arrayOf(
            MediaStore.Images.Media._ID, // Include file ID in projection
            MediaStore.Images.Media.DATA
        )
        val selection = "${MediaStore.Images.Media.DATA} LIKE '$folderPath%'"
        // Change this to match your directory
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )

        cursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                val filepath =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                val statusModel = StatusModel().apply {
                    this.filepath = filepath
                    this.id = id
                } // Create StatusModel object with filepath and selected status
                cloneList.add(statusModel)
            }
        }
        whatsAppImages.postValue(cloneList)
    }

    private fun getWhatsAppVideos(context: Context) {
        val cloneList: MutableList<StatusModel> = java.util.ArrayList()
        // Define the folder path
        val folderPath =
            "/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Video"

        val contentResolver = context.contentResolver
        val projection = arrayOf(
            MediaStore.Video.Media._ID, // Include file ID in projection
            MediaStore.Video.Media.DATA
        )
        val selection = "${MediaStore.Video.Media.DATA} LIKE '$folderPath%'"
        // Change this to match your directory
        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )

        cursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                val filepath =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                val statusModel = StatusModel().apply {
                    this.filepath = filepath
                    this.id = id
                } // Create StatusModel object with filepath and selected status
                cloneList.add(statusModel)
            }
        }
        whatsAppVideos.postValue(cloneList)
    }

    private fun getWhatsAppDocument(context: Context) {
        val cloneList: MutableList<AudioModel> = java.util.ArrayList()
        // Define the folder path
        val folderPath =
            "/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents"

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID, // Include file ID in projection
            MediaStore.Files.FileColumns.DATA
        )
        val contentResolver = context.contentResolver
        val selection = "${MediaStore.Files.FileColumns.DATA} LIKE '$folderPath%'"
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        // Change this to match your directory
        val cursor = contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val id =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                val filepath =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))
                val documentFile = File(filepath)
                val doc =
                    AudioModel(
                        documentFile,
                        id,
                        filepath,
                        documentFile.lastModified()
                    ); // Create StatusModel object with filepath and selected status
                cloneList.add(doc)
            }
        }
        whatsAppDocument.postValue(cloneList)
    }

    private fun getWhatsAppAudios(context: Context) {
        val cloneList: MutableList<AudioModel> = java.util.ArrayList()
        // Define the folder path
        val folderPath =
            "/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Audio"

        val projection = arrayOf(
            MediaStore.Audio.Media._ID, // Include file ID in projection
            MediaStore.Audio.Media.DATA
        )
        val contentResolver = context.contentResolver
        val selection = "${MediaStore.Audio.Media.DATA} LIKE '$folderPath%'"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        // Change this to match your directory
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val filepath =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val documentFile = File(filepath)
                val doc =
                    AudioModel(
                        documentFile,
                        id,
                        filepath,
                        documentFile.lastModified()
                    ); // Create StatusModel object with filepath and selected status
                cloneList.add(doc)
            }
        }
        whatsAppAudio.postValue(cloneList)
    }

}