package com.statussaver.dele.ui.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.statussaver.dele.R
import com.statussaver.dele.database.PrefManager
import com.statussaver.dele.databinding.ActivityPermissionBinding
import com.statussaver.dele.services.DataService
class PermissionActivity : AppCompatActivity() {

    private val PERMISSION_CODE = 32141
    private val MY_IGNORE_OPTIMIZATION_REQUEST = 23232
    var pm: PowerManager? = null
    lateinit var prefManager: PrefManager
    private lateinit var binding: ActivityPermissionBinding

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

        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()


        binding.next.setOnClickListener {
            setRequestPermissions()
        }

        binding.allow.setOnClickListener {
            if (checkPermissions())
                if (pm!!.isIgnoringBatteryOptimizations(packageName))
                    notificationPermission()
                else
                    batteryOptimisationIntent()
            else
                requestPermissions()

        }

        setRequestPermissions()
    }

    fun init(){
        pm = this.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    private fun notificationPermission(){
        if (prefManager.getIsPermissionSet()!!) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                val `in` = Intent(
                    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
                )
                `in`.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                applicationContext.startActivity(`in`)
            }
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO
            ).filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()
        } else {
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ).filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()
        }

        ActivityCompat.requestPermissions(this, permissionsToRequest, PERMISSION_CODE)
    }

    private fun checkPermissions() : Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            return  arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO
            ).all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            return arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    startDataService()
                    binding.allow.visibility = View.GONE
                }
            }
        }
    }

    private fun startDataService(){
        val serviceIntent = Intent(this, DataService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        }else{
            startService(serviceIntent)
        }
    }

    private fun batteryOptimisationIntent() {
        try {
            val intent = Intent()
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, MY_IGNORE_OPTIMIZATION_REQUEST)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_IGNORE_OPTIMIZATION_REQUEST && resultCode == RESULT_OK) {
            binding.allow.visibility = View.GONE
        } else {
            Toast.makeText(this, "Permission not given", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setRequestPermissions(){
        if (binding.textView6.text.equals("Notification Listener"))
            if (prefManager.getIsPermissionSet()!!)
                binding.allow.visibility = View.GONE
            else
                binding.allow.visibility = View.VISIBLE
        else
            binding.allow.visibility = View.VISIBLE
        if (checkPermissions())
        {
            if (pm!!.isIgnoringBatteryOptimizations(packageName)){
                if (prefManager.getIsPermissionSet()!!)
                {
                    startActivity(Intent(applicationContext,MainActivity::class.java))
                    finish()
                }else{
                    binding.textView6.text = "Notification Listener"
                    binding.textView7.text = ""
                    binding.imageView5.setImageDrawable(applicationContext.resources.getDrawable(R.drawable.smartphone))
                }
            }else{
                binding.textView6.text = "Battery Optimisations"
                binding.textView7.text = ""
                binding.imageView5.setImageDrawable(applicationContext.resources.getDrawable(R.drawable.battery_level))
            }
        }else{
            binding.textView6.text = "Storage Permission"
            binding.textView7.text = ""
            binding.imageView5.setImageDrawable(applicationContext.resources.getDrawable(R.drawable.folder))
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.textView6.text.equals("Notification Listener"))
            if (prefManager.getIsPermissionSet()!!)
                binding.allow.visibility = View.GONE
    }
}