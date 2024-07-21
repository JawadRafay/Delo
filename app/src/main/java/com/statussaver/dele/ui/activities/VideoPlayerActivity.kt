package com.statussaver.dele.ui.activities

import android.content.res.Configuration
import android.os.Bundle
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.statussaver.dele.R
import com.statussaver.dele.database.PrefManager
import com.statussaver.dele.databinding.ActivityVideoPlayerBinding
import com.statussaver.dele.utils.Utils
class VideoPlayerActivity : AppCompatActivity() {

    private var videoPath: String? = null
    lateinit var prefManager: PrefManager
    private lateinit var binding : ActivityVideoPlayerBinding

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

        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoPath = Utils.mPath

        binding.backIV.setOnClickListener {
            finish()
        }

        binding.displayVV.setVideoPath(videoPath)

        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.displayVV)

        binding.displayVV.setMediaController(mediaController)

        binding.displayVV.start()
    }

    override fun onResume() {
        super.onResume()
        binding.displayVV.setVideoPath(videoPath)
        binding.displayVV.start()
    }

    override fun onPause() {
        super.onPause()
        binding.displayVV.pause()
    }
}