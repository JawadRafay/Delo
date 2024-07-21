package com.statussaver.dele.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import com.statussaver.dele.R
import com.statussaver.dele.database.PrefManager
import com.statussaver.dele.model.AudioModel
import com.statussaver.dele.model.StatusModel
import com.statussaver.dele.utils.PathDirectories
import com.statussaver.dele.utils.Utils
import org.apache.commons.io.comparator.LastModifiedFileComparator
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*

class DataService : Service() {

    private val TAG = "DataService"
    private lateinit var dirAudios: File
    private lateinit var dirVoices: File
    private lateinit var dirImages: File
    private lateinit var dirVideos: File
    private lateinit var dirDocuments: File
    private lateinit var dirDownloadPhotos: File
    private lateinit var dirDownloadVideos: File
    var prefManager: PrefManager? = null
    private var isPhotosMoved = false
    private var isVideosMoved = false
    private var isAudiosMoved = false
    private var isVoicesMoved = false
    private var isDocsMoved = false

    companion object {
        val whatsapp_docs = MutableLiveData<List<AudioModel>>()
        val whatsapp_voices = MutableLiveData<List<AudioModel>>()
        val whatsapp_audios = MutableLiveData<List<AudioModel>>()
        val whatsapp_image = MutableLiveData<List<StatusModel>>()
        val whatsapp_videos = MutableLiveData<List<StatusModel>>()
        val whatsapp_status = MutableLiveData<List<StatusModel>>()
        val whatsapp_status_download_photos = MutableLiveData<List<StatusModel>>()
        val whatsapp_status_download_videos = MutableLiveData<List<StatusModel>>()
    }


    override fun onCreate() {
        super.onCreate()
        prefManager = PrefManager(this)
        makeDeloDirectories()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        isPhotosMoved = false
        isVideosMoved = false
        isAudiosMoved = false
        isVoicesMoved = false
        isDocsMoved = false

        Thread { getWhatsappStatus() }.start()

        Thread { movePhotoData() }.start()

        Thread { moveAudioData() }.start()

        Thread { moveVideoData() }.start()

        Thread { moveVoiceData() }.start()

        Thread { moveDocumentData() }.start()

        Thread { getDownloadPhotos() }.start()

        Thread { getDownloadVideos() }.start()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(applicationContext, DataServiceAboveTenVersion::class.java))
            startService(Intent(applicationContext, DataServiceAboveTenVersion::class.java))
        }

        return START_NOT_STICKY
    }


    //photos
    private fun movePhotoData() {
        val file: File = PathDirectories.getWhatsappImages()
        val listFiles = listOf(*file.listFiles())
        Collections.sort(listFiles, fileComparator())
        var check = false
        for (pathname in listFiles) {
            if (check)
                break
            if (pathname.isFile) {
                Utils.compareDates(prefManager?.getDate(), pathname.lastModified())?.let {
                    if (it) {
                        val destinationFile = File(dirImages.getPath() + "/" + pathname.name)
                        if (!destinationFile.exists()) {
                            try {
                                copyFile(pathname, destinationFile)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    } else
                        check = true
                }
            }
        }
        getFromOwnDirectory()
    }

    private fun getFromOwnDirectory() {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            var downloadPhotos: Array<File?>
            val f = File(
                Environment.getExternalStorageDirectory().absolutePath + "WhatsApp/Media/WhatsApp Images")
            downloadPhotos = f.listFiles() as Array<File?>

            Log.d("Images", "getFromOwnDirectory: " + downloadPhotos)

            downloadPhotos?.let {
                Arrays.sort(it, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                Log.d("Images", "getFromOwnDirectoryIt: " + it)
                for (value in it) {
                    Log.d("Images", "getFromOwnDirectoryValue: " + value)
                    if (value != null) {
                        if (value.isFile) {
                            val model = StatusModel()
                            model.filepath = value.absolutePath
                            Log.d("Images", "getFromOwnDirectoryModel: " + model.filepath)
                            cloneList.add(model)
                        }
                    }
                }
            }
        } else {*/
            val cloneList: MutableList<StatusModel> = ArrayList()
            if (dirImages.isDirectory) {
                val listFile = dirImages.listFiles()
                listFile?.let {
                    Arrays.sort(it, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                    for (value in it) {
                        if (value.isFile) {
                            val model = StatusModel()
                            model.filepath = value.absolutePath
                            cloneList.add(model)
                        }
                    }
                }
            }
        //}
        whatsapp_image.postValue(cloneList)
        isPhotosMoved = true
        updateDate()
    }

    //Audios
    private fun moveAudioData() {
        var file: File? = null
        if (PathDirectories.AUDIO_DIRECTORY.exists()) {
            file = PathDirectories.AUDIO_DIRECTORY
        } else if (PathDirectories.AUDIO_DIRECTORY_NEW.exists()) {
            file = PathDirectories.AUDIO_DIRECTORY_NEW
        }
        file?.let {
            val listFiles = listOf(*it.listFiles())
            Collections.sort(listFiles, fileComparator())
            var check = false
            for (pathname in listFiles) {
                if (check)
                    break
                if (pathname.isFile) {
                    Utils.compareDates(prefManager?.getDate(), pathname.lastModified())
                        ?.let { isToday ->
                            if (isToday) {
                                if (pathname.name.endsWith(".m4a")
                                    || pathname.name.endsWith(".opus")
                                    || pathname.name.endsWith(".mp3")
                                ) {
                                    val destinationFile =
                                        File(dirAudios.getPath() + "/" + pathname.name)
                                    if (!destinationFile.exists()) {
                                        try {
                                            copyFile(pathname, destinationFile)
                                        } catch (e: IOException) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            } else
                                check = true
                        }
                }
            }
        }
        getFromOwnAudioDirectory()
    }

    private fun getFromOwnAudioDirectory() {
        val cloneList: MutableList<AudioModel> = ArrayList()
        if (dirAudios.isDirectory) {
            val listFile = dirAudios.listFiles()
            listFile?.let {
                Arrays.sort(it, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                for (value in it) {
                    if (value.isFile) {
                        val audio = AudioModel(
                            value,
                            value.name,
                            value.absolutePath,
                            value.lastModified()
                        )
                        cloneList.add(audio)
                    }
                }
            }
        }
        whatsapp_audios.postValue(cloneList)
        isAudiosMoved = true
        updateDate()
    }

    //videos
    private fun moveVideoData() {
        val file: File = PathDirectories.getWhatsappVideosFolder()
        val listFiles = listOf(*file.listFiles())
        Collections.sort(listFiles, fileComparator())
        var check = false
        for (pathname in listFiles) {
            if (check)
                break
            if (pathname.isFile) {
                Utils.compareDates(prefManager?.getDate(), pathname.lastModified())?.let {
                    if (it) {
                        val destinationFile = File(dirVideos.getPath() + "/" + pathname.name)
                        if (!destinationFile.exists()) {
                            try {
                                copyFile(pathname, destinationFile)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    } else
                        check = true
                }
            }
        }
        getFromOwnVideoDirectory()
    }

    private fun getFromOwnVideoDirectory() {
        val cloneList: MutableList<StatusModel> = ArrayList()
        if (dirVideos.isDirectory) {
            val listFile = dirVideos.listFiles()
            listFile?.let {
                Arrays.sort(it, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                for (value in it) {
                    if (value.isFile) {
                        val model = StatusModel()
                        model.filepath = value.absolutePath
                        cloneList.add(model)
                    }
                }
            }
        }
        whatsapp_videos.postValue(cloneList)
        isVideosMoved = true
        updateDate()
    }

    //Voices
    private fun moveVoiceData() {
        var file: File? = null
        if (PathDirectories.VOICE_DIRECTORY.exists()) {
            file = PathDirectories.VOICE_DIRECTORY
        } else if (PathDirectories.VOICE_DIRECTORY_NEW.exists()) {
            file = PathDirectories.VOICE_DIRECTORY_NEW
        }
        var check = false
        file?.let {
            val listDirs = listOf(*it.listFiles())
            Collections.sort(listDirs, fileComparator())
            for (dir in listDirs) {
                if (check)
                    break
                if (dir.listFiles() != null) {
                    val listFiles = listOf(*dir.listFiles())
                    Collections.sort(listFiles, fileComparator())
                    for (pathname in listFiles) {
                        if (check)
                            break
                        if (!pathname.name.equals(".nomedia")) {
                            Utils.compareDates(prefManager?.getDate(), pathname.lastModified())
                                ?.let { isToday ->
                                    if (isToday) {
                                        if (pathname.name.endsWith(".m4a")
                                            || pathname.name.endsWith(".opus")
                                            || pathname.name.endsWith(".mp3")
                                        ) {
                                            val destinationFile =
                                                File(dirVoices.getPath() + "/" + pathname.name)
                                            if (!destinationFile.exists()) {
                                                try {
                                                    copyFile(pathname, destinationFile)
                                                } catch (e: IOException) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                    } else
                                        check = true
                                }
                        }
                    }
                }
            }
        }
        getFromOwnVoiceDirectory()
    }

    private fun getFromOwnVoiceDirectory() {
        val cloneList: MutableList<AudioModel> = ArrayList()
        if (dirVoices.isDirectory) {
            val listFile = dirVoices.listFiles()
            listFile?.let {
                Arrays.sort(it, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                for (value in it) {
                    if (value.isFile) {
                        val audio = AudioModel(
                            value,
                            value.name,
                            value.absolutePath,
                            value.lastModified()
                        )
                        cloneList.add(audio)
                    }
                }
            }
        }
        whatsapp_voices.postValue(cloneList)
        isVoicesMoved = true
        updateDate()
    }

    //Document
    private fun moveDocumentData() {

        var file: File? = null
        if (PathDirectories.DOC_DIRECTORY.exists()) {
            file = PathDirectories.DOC_DIRECTORY
        } else if (PathDirectories.DOC_DIRECTORY_NEW.exists()) {
            file = PathDirectories.DOC_DIRECTORY_NEW
        }
        file?.let {
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm")
            val listFiles = listOf(*it.listFiles())
            Collections.sort(listFiles, fileComparator())
            var check = false
            for (pathname in listFiles) {
                if (check)
                    break
                if (pathname.isFile) {
                    Utils.compareDates(prefManager?.getDate(), pathname.lastModified())?.let {
                        if (it) {
                            val destinationFile = File(dirDocuments.getPath() + "/" + pathname.name)
                            if (!destinationFile.exists()) {
                                try {
                                    copyFile(pathname, destinationFile)
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                        } else
                            check = true
                    }
                }
            }
        }
        getFromOwnDocumentDirectory()
    }

    private fun getFromOwnDocumentDirectory() {
        val cloneList: MutableList<AudioModel> = ArrayList()
        if (dirDocuments.isDirectory) {
            val listFile = dirDocuments.listFiles()
            listFile?.let {
                Arrays.sort(it, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                for (value in it) {
                    if (value.isFile) {
                        val doc =
                            AudioModel(value, value.name, value.absolutePath, value.lastModified());
                        cloneList.add(doc)
                    }
                }
            }
        }
        whatsapp_docs.postValue(cloneList)
        isDocsMoved = true
        updateDate()
    }

    //Status
    private fun getWhatsappStatus() {
        val file: File = PathDirectories.getWhatsappStatusFolder()
        val cloneList: MutableList<StatusModel> = ArrayList()

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            var downloadPhotos: Array<File?>
            val f = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.getExternalStorageState()
                ).toString() + File.separator + "WhatsApp/Media/.Statuses")

            downloadPhotos = file.listFiles() as Array<File?>
            Log.d(TAG, "downloadPhotos: "+ downloadPhotos)

            downloadPhotos.let {
                Arrays.sort(it, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                Log.d(TAG, "CheckStatusIt: $it")
                for (value in it) {
                    if (value != null) {
                        if (value.isFile) {
                            val model = StatusModel()
                            model.filepath = value.absolutePath
            Log.d(TAG, "downloadPhotosValue: "+ value)
                            cloneList.add(model)
                        }
                    }
                }
            }
        } else {*/
        if (file.isDirectory) {
            val listFile = file.listFiles()
            if (listFile != null) {
                Arrays.sort(listFile, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                for (i in listFile) {
                    if (!i.name.equals(".nomedia")) {
                        if (i.isFile) {
                            val model = StatusModel()
                            Log.d("check_tag", "CheckDataServices: $i")
                            model.filepath = i.toUri().toString()
                            cloneList.add(model)
                        }
                    }
                }
            }
        }
        //}
        whatsapp_status.postValue(cloneList)
    }

    //download photos
    private fun getDownloadPhotos() {
        val cloneList: MutableList<StatusModel> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            var downloadPhotos: Array<File?>
            val f1 = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM
                ).toString() + File.separator + "Dele Statuses"
            )
            downloadPhotos = f1.listFiles() as Array<File?>

            downloadPhotos?.let {
                Arrays.sort(it, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                for (value in it) {
                    if (value != null) {
                        if (value.isFile) {
                            val model = StatusModel()
                            model.filepath = value.absolutePath
                            if (Utils.getBack(
                                    model.filepath,
                                    "((\\.jpg|\\.png|\\.gif|\\.jpeg|\\.bmp)$)"
                                )!!.isNotEmpty()
                            ) {
                                cloneList.add(model)
                            }
                        }
                    }
                }
            }
            // Second directory: Dele/Images
            val f2 = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                ).toString() + File.separator + "Dele/Images"
            )
            downloadPhotos = f2.listFiles() as Array<File?>
            downloadPhotos?.let {
                Arrays.sort(it, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                for (value in it) {
                    if (value != null && value.isFile) {
                        val model = StatusModel()
                        model.filepath = value.absolutePath
                        if (Utils.getBack(
                                model.filepath,
                                "((\\.jpg|\\.png|\\.gif|\\.jpeg|\\.bmp)$)"
                            )!!.isNotEmpty()
                        ) {
                            cloneList.add(model)
                        }
                    }
                }
            }
        } else {
            if (dirDownloadPhotos.isDirectory) {
                val listFile = dirDownloadPhotos.listFiles()
                listFile?.let {
                    Arrays.sort(it, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                    for (value in it) {
                        if (value.isFile) {
                            val model = StatusModel()
                            model.filepath = value.absolutePath
                            cloneList.add(model)
                        }
                    }
                }
            }
        }
        whatsapp_status_download_photos.postValue(cloneList)
    }

    //download photos
    private fun getDownloadVideos() {
        val cloneList: MutableList<StatusModel> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            var downloadPhotos: Array<File?>
            val f1 = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM
                ).toString() + File.separator + "Dele Statuses"
            )
            downloadPhotos = f1.listFiles() as Array<File?>

            downloadPhotos?.let {
                Arrays.sort(it, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                for (value in it) {
                    if (value != null) {
                        if (value.isFile) {
                            val model = StatusModel()
                            model.filepath = value.absolutePath
                            if (Utils.getBack(
                                    model.filepath,
                                    "((\\.mp4|\\.webm|\\.ogg|\\.mpK|\\.avi|\\.mkv|\\.flv|\\.mpg|\\.wmv|\\.vob|\\.ogv|\\.mov|\\.qt|\\.rm|\\.rmvb\\.|\\.asf|\\.m4p|\\.m4v|\\.mp2|\\.mpeg|\\.mpe|\\.mpv|\\.m2v|\\.3gp|\\.f4p|\\.f4a|\\.f4b|\\.f4v)$)"
                                )!!.isNotEmpty()
                            ) {
                                cloneList.add(model)
                            }
                        }
                    }
                }
            }

            // Second directory: Dele/Videos
            val f2 = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                ).toString() + File.separator + "Dele/Videos"
            )
            downloadPhotos = f2.listFiles() as Array<File?>
            downloadPhotos?.let {
                Arrays.sort(it, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                for (value in it) {
                    if (value != null && value.isFile) {
                        val model = StatusModel()
                        model.filepath = value.absolutePath
                        if (Utils.getBack(
                                model.filepath,
                                "((\\.mp4|\\.webm|\\.ogg|\\.mpK|\\.avi|\\.mkv|\\.flv|\\.mpg|\\.wmv|\\.vob|\\.ogv|\\.mov|\\.qt|\\.rm|\\.rmvb\\.|\\.asf|\\.m4p|\\.m4v|\\.mp2|\\.mpeg|\\.mpe|\\.mpv|\\.m2v|\\.3gp|\\.f4p|\\.f4a|\\.f4b|\\.f4v)\$)"
                            )!!.isNotEmpty()
                        ) {
                            cloneList.add(model)
                        }
                    }
                }
            }
        } else {
            if (dirDownloadVideos.isDirectory) {
                val listFile = dirDownloadVideos.listFiles()
                listFile?.let {
                    Arrays.sort(it, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                    for (value in it) {
                        if (value.isFile) {
                            val model = StatusModel()
                            model.filepath = value.absolutePath
                            cloneList.add(model)
                        }
                    }
                }
            }
        }
        whatsapp_status_download_videos.postValue(cloneList)
    }

    //common functions
    private fun getDate(): Long {
        val calendar = Calendar.getInstance()
        return calendar.timeInMillis
    }

    @Throws(IOException::class)
    fun copyFile(sourceFile: File?, destFile: File) {
        if (!destFile.parentFile.exists())
            destFile.parentFile?.mkdirs()

        if (!destFile.exists()) {
            destFile.createNewFile()
        }
        var source: FileChannel? = null
        var destination: FileChannel? = null
        try {
            source = FileInputStream(sourceFile).channel
            destination = FileOutputStream(destFile).channel
            destination.transferFrom(source, 0, source.size())
        } finally {
            source?.close()
            destination?.close()
        }
    }

    private fun makeDeloDirectories() {
        val sd = Environment.getExternalStorageDirectory()

        dirImages = File(sd, File.separator + getString(R.string.app_name) + "/Images/")
        if (!dirImages.exists()) dirImages.mkdirs()

        dirVideos = File(sd, File.separator + getString(R.string.app_name) + "/Videos/")
        if (!dirVideos.exists()) dirVideos.mkdirs()

        dirAudios = File(sd, File.separator + getString(R.string.app_name) + "/Audios/")
        if (!dirAudios.exists()) dirAudios.mkdirs()

        dirVoices = File(sd, File.separator + getString(R.string.app_name) + "/Voices/")
        if (!dirVoices.exists()) dirVoices.mkdirs()

        dirDocuments = File(sd, File.separator + getString(R.string.app_name) + "/Documents/")
        if (!dirDocuments.exists()) dirDocuments.mkdirs()

        dirDownloadPhotos = File(
            sd,
            File.separator + "Download" + File.separator + getString(R.string.app_name) + "/Images/"
        )
        if (!dirDownloadPhotos.exists()) dirDownloadPhotos.mkdirs()

        dirDownloadVideos = File(
            sd,
            File.separator + "Download" + File.separator + getString(R.string.app_name) + "/Videos/"
        )
        if (!dirDownloadVideos.exists()) dirDownloadVideos.mkdirs()
    }

    class fileComparator : Comparator<File> {
        override fun compare(o1: File?, o2: File?): Int {
            return o2?.lastModified().toString().compareTo(o1?.lastModified().toString())
        }
    }

    //notification
    @SuppressLint("ForegroundServiceType")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "18361"
        val channelName = "Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(true)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentTitle("")
            .build()
        startForeground(12711, notification)
    }

    private fun updateDate() {
        if (isVoicesMoved and isAudiosMoved and isVideosMoved and isPhotosMoved and isDocsMoved)
            prefManager?.setDate(getDate())
    }
}