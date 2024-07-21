package com.statussaver.dele.ui.activities

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.statussaver.dele.R
import com.statussaver.dele.database.PrefManager
import com.statussaver.dele.services.DataService
import com.statussaver.dele.utils.Utils


class Splash : AppCompatActivity() {

    var pm: PowerManager? = null
    lateinit var prefManager:PrefManager

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefManager = PrefManager(this)
        val nightModeFlags = applicationContext.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
            if (prefManager.getDarkMode())
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        setContentView(R.layout.activity_splash)
        init()

        if (prefManager.getDate() == 0L)
            prefManager.setDate(Utils.getDate())

        if(checkAndRequestPermissions()
            && pm!!.isIgnoringBatteryOptimizations(packageName)
            && prefManager.getIsPermissionSet()!!
        ){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(applicationContext,DataService::class.java))
            }else{
                startService(Intent(applicationContext,DataService::class.java))
            }
            val animator = ValueAnimator.ofInt(0, 100)
            animator.duration = 1800
            animator.addUpdateListener {
                if (it.animatedValue.toString() == "100")
                {
                    startActivity(Intent(applicationContext,MainActivity::class.java))
                    finish()
                }
            }
            animator.start()
        }else{
            startActivity(Intent(applicationContext,PermissionActivity::class.java))
            finish()
        }
    }

    fun checkAndRequestPermissions() : Boolean {
        val write =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val read =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (write != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return listPermissionsNeeded.isEmpty()
    }

    fun init(){
        pm = this.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

}