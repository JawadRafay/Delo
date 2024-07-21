package com.statussaver.dele.ui.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.statussaver.dele.R
import com.statussaver.dele.database.PrefManager
import com.statussaver.dele.databinding.ActivitySettingsBinding
class SettingsActivity : AppCompatActivity() {

    lateinit var prefManager: PrefManager;
    private lateinit var binding: ActivitySettingsBinding

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

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.rateUs.setOnClickListener {
            rateTheApp()
        }

        binding.rateUsTap.setOnClickListener {
            rateTheApp()
        }

        binding.share.setOnClickListener {
            shareApp()
        }

        binding.shareTap.setOnClickListener {
            shareApp()
        }

        binding.moreApps.setOnClickListener {
            moreApps()
        }

        binding.moreAppsTap.setOnClickListener {
            moreApps()
        }

        binding.switchDarkMode.isChecked = prefManager.getDarkMode()

        binding.switchDarkMode.setOnCheckedChangeListener { buttonView, isChecked ->
            prefManager.setDarkMode(isChecked)
            recreate()
        }
    }

    private fun rateTheApp() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + this.packageName)
                )
            )
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + this.packageName)
                )
            )
        }
    }

    private fun shareApp() {
        val sharelink = Intent(Intent.ACTION_SEND)
        sharelink.type = "text/*"
        sharelink.putExtra(
            Intent.EXTRA_SUBJECT,
            applicationContext.resources.getString(R.string.app_name) + " (Download it From play store)"
        )
        sharelink.putExtra(
            Intent.EXTRA_TEXT,
            "http://play.google.com/store/apps/details?id=$packageName"
        )
        startActivity(Intent.createChooser(sharelink, "Share with friends via "))
    }

    private fun moreApps() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://search?q=pub:" + resources.getString(R.string.dev_id))
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/search?q=pub:" + resources.getString(R.string.dev_id))
                )
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_OK,intent)
        finish()
    }

}