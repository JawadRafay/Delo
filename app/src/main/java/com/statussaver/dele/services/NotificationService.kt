package com.statussaver.dele.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.media.RingtoneManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.statussaver.dele.R
import com.statussaver.dele.database.PrefManager
import com.statussaver.dele.model.ContactModel
import com.statussaver.dele.model.NotificationModel
import com.statussaver.dele.ui.activities.Splash
import com.statussaver.dele.utils.RefreshListener
import com.statussaver.dele.utils.SqliteHelper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.regex.Pattern

class NotificationService : NotificationListenerService() {

    private val TAG = NotificationService::class.java.simpleName
    var sqliteHelper: SqliteHelper? = null
    var appPackage : String? = null
    var nTicker = ""
    var text = ""
    var title:String = ""
    var icon: Icon? = null
    var drawable: Drawable? = null
    var postTime: Long = 0
    var currentTime: Long = 0
    var checkTime: Boolean? = null
    val regx1 = "([0-9].(new).(messages))"
    val regx2 = "(.[0-9].(messages).)"
    lateinit var prefManager: PrefManager

    companion object{
        var refreshListener: RefreshListener? = null
        var notificationModels: MutableList<NotificationModel>? = null
    }

    override fun onCreate() {
        super.onCreate()
        prefManager = PrefManager(this)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        notificationModels = ArrayList<NotificationModel>()
        sqliteHelper = SqliteHelper(applicationContext)
        prefManager.setIsPermissionSet(true)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        prefManager.setIsPermissionSet(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        appPackage = sbn?.packageName

        if (appPackage.equals("com.whatsapp")){
            val extras = sbn!!.notification.extras

            if (extras.getString("android.title") != null)
                title = extras.getString("android.title")!!
            else
                return
            text = extras.getCharSequence("android.text").toString()

            if (text == "This message was deleted"){
                showNotification(title)
                return
            }

            postTime = sbn.notification.`when`
            currentTime = System.currentTimeMillis()
            var bmp: Bitmap? = null
            if (extras.containsKey(Notification.EXTRA_LARGE_ICON)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    icon = extras[Notification.EXTRA_LARGE_ICON] as Icon?
                else
                    bmp = extras[Notification.EXTRA_LARGE_ICON] as Bitmap?
            }

            val pattern2 = Pattern.compile(regx2)
            val matcher2 = pattern2.matcher(title)

            if (title.contains(":")) {
                val tt = matcher2.replaceFirst("")
                val tt2 = tt.split(":").toTypedArray()
                title = tt2[0].replace("(", "").replace(")", "").trim { it <= ' ' }
                if (tt2[1].trim { it <= ' ' } == "You") return
                text = tt2[1].trim { it <= ' ' } + ": " + text
            } else {
                if (title == "You") return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (icon != null) {
                    var isAvailable = false
                    for (model in notificationModels!!) {
                        if (model.name.equals(title)) {
                            isAvailable = true
                            break
                        }
                    }
                    if (!isAvailable) {
                        notificationModels?.add(
                            NotificationModel(
                                title,
                                icon,
                                sbn.notification
                            )
                        )
                    }
                    val model = ContactModel(
                        nTicker, title,
                        "", text, postTime, "other"
                    )
                    sqliteHelper!!.addContactID(model)
                    refreshListener?.onRefresh(model)
                }
            } else {
                Log.d(TAG, "onNotificationPosted:else " + (bmp == null))
                if (bmp != null) {
                    var isAvailable = false
                    for (model in notificationModels!!) {
                        if (model.name.equals(title)) {
                            isAvailable = true
                            break
                        }
                    }
                    if (!isAvailable) {
                        notificationModels?.add(
                            NotificationModel(
                                title,
                                null,
                                sbn.notification
                            )
                        )
                    }
                    val model = ContactModel(
                        nTicker, title,
                        saveProfile(bmp, title), text, postTime, "other"
                    )
                    sqliteHelper!!.addContactID(model)
                    refreshListener?.onRefresh(model)
                }
            }
        }
    }

    fun setListener(myListener: RefreshListener) {
        NotificationService.refreshListener = myListener
    }

    private fun saveProfile(btp: Bitmap?, file: String): String? {
        return if (btp != null) {
            val f = File(applicationContext.cacheDir, file)
            if (f.exists()) {
                return f.absolutePath
            }
            var fos: FileOutputStream? = null
            try {
                f.createNewFile()
                val bitmap: Bitmap = btp
                val bos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100 , bos)
                val bitmapdata = bos.toByteArray()
                fos = FileOutputStream(f)
                fos.write(bitmapdata)
                fos.flush()
                fos.close()
                Log.d(TAG, "saveProfile: " + f.absolutePath)
                return f.absolutePath
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "saveProfile: " + e.message)
            }
            null
        } else {
            null
        }
    }

    private fun showNotification(name:String = "") {
        val ID = "21393"
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val intent = Intent(this, Splash::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val time = System.currentTimeMillis()
        val builder: Notification.Builder = Notification.Builder(this)
            .setAutoCancel(true)
            .setWhen(time)
            .setShowWhen(true)
            .setSound(alarmSound)
            .setContentText("Tap to see $name deleted message")
            .setContentTitle("Message Deleted")
            .setContentIntent(pendingIntent)
            .setStyle(
                Notification.BigTextStyle().bigText("Tap to see $name deleted message")
            )
            .setSmallIcon(R.mipmap.ic_launcher)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ID,
                "Dele Notification Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
            builder.setChannelId(ID)
        }
        manager.notify(34781, builder.build())
    }
}