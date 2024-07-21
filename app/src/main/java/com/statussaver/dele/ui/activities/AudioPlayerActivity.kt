package com.statussaver.dele.ui.activities

import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.statussaver.dele.R
import com.statussaver.dele.database.PrefManager
import com.statussaver.dele.databinding.ActivityAudioPlayerBinding
import com.statussaver.dele.model.AudioModel

import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class AudioPlayerActivity : AppCompatActivity() {

    var imageList: ArrayList<AudioModel>? = null
    var position = 0
    var hdlr: Handler? = null
    var mPlayer: MediaPlayer? = null
    lateinit var prefManager: PrefManager
    private lateinit var binding: ActivityAudioPlayerBinding

    companion object{
        var sTime: Int = 0
        var eTime: Int = 0
        var fTime: Int = 10000
        var bTime: Int = 10000
        var oTime: Int = 0

    }

    private val TAG = "AudioPlayerActivity"

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
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sBar.isClickable = false
        mPlayer = MediaPlayer()
        val intent = intent
        if (intent != null) {
            try {
                mPlayer!!.setDataSource(intent.getStringExtra("path"))
                mPlayer!!.prepare()
                mPlayer!!.setOnCompletionListener {
                    binding.btnPlay.setImageResource(R.drawable.ic_play)
                }
                playAudio()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            binding.txtSname.text = intent.getStringExtra("name")
            imageList = getIntent().getParcelableArrayListExtra("imagesList")
            position = getIntent().getIntExtra("position", 0)
        }

        binding.btnPlay.setOnClickListener {
            if (mPlayer!!.isPlaying) {
                mPlayer!!.pause()
                hdlr!!.removeCallbacks(UpdateSongTime)
                binding.btnPlay.setImageResource(R.drawable.ic_play)
            } else {
                playAudio()
            }
        }

        binding.btnForward.setOnClickListener {
            position += 1
            binding.txtSname.text = imageList!![position].title
            if (mPlayer != null && mPlayer!!.isPlaying) {
                mPlayer!!.stop()
                hdlr!!.removeCallbacks(UpdateSongTime)
            }
            try {
                mPlayer!!.reset()
                mPlayer!!.setDataSource(imageList!![position].paths)
                mPlayer!!.prepare()
                playAudio()
            } catch (e: Exception) {
                Log.d(TAG, "onClick: Forward..." + e.message)
            }
        }

        binding.btnBackward.setOnClickListener {
            position -= 1
            binding.txtSname.text = imageList!![position].title
            if (mPlayer != null && mPlayer!!.isPlaying) {
                mPlayer!!.stop()
                hdlr!!.removeCallbacks(UpdateSongTime)
            }
            try {
                mPlayer!!.reset()
                mPlayer!!.setDataSource(imageList!![position].paths)
                mPlayer!!.prepare()
                playAudio()
            } catch (e: java.lang.Exception) {
                Log.d(TAG, "onClick: Backward..." + e.message)
            }
        }

        binding.btnFastForward.setOnClickListener {
            if (sTime + fTime <= eTime) {
                sTime += fTime
                mPlayer!!.seekTo(sTime)
            }
            if (!binding.btnPlay.isEnabled) {
                binding.btnPlay.isEnabled = true
            }
        }

        binding.btnFastBackward.setOnClickListener {
            if (sTime - bTime > 0) {
                sTime -= bTime
                mPlayer!!.seekTo(sTime)
            }
            if (!binding.btnPlay.isEnabled) {
                binding.btnPlay.isEnabled = true
            }
        }

        binding.backIV.setOnClickListener {
            finish()
        }

        binding.sBar.progressChangedCallback = {
            val current = it * mPlayer!!.duration;
            mPlayer!!.seekTo(current.roundToInt())
        }
    }

    private fun playAudio() {
        hdlr = Handler(Looper.getMainLooper())
        mPlayer!!.start()
        eTime = mPlayer!!.duration
        sTime = mPlayer!!.currentPosition
        if (oTime == 0) {
            oTime = 1
        }
        binding.txtSongTime.text = String.format(
            "%d:%d", TimeUnit.MILLISECONDS.toMinutes(eTime.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(eTime.toLong()) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    eTime.toLong()
                )
            )
        )
        binding.txtStartTime.text = String.format(
            "%dd:%dd", TimeUnit.MILLISECONDS.toMinutes(sTime.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(sTime.toLong()) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    sTime.toLong()
                )
            )
        )

        hdlr!!.postDelayed(UpdateSongTime, 0)
        binding.btnPlay.setImageResource(R.drawable.ic_pause)
    }

    private val UpdateSongTime: Runnable = object : Runnable {
        override fun run() {
            sTime = mPlayer!!.currentPosition
            binding.txtStartTime.text = String.format(
                "%d:%d", TimeUnit.MILLISECONDS.toMinutes(sTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(sTime.toLong()) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(
                        sTime.toLong()
                    )
                )
            )

            val progress = sTime.toString().toFloat()/ eTime.toString().toFloat()
            binding.sBar.setProgress(progress)

            if (sTime != eTime) {
                hdlr!!.postDelayed(this, 500)
            } else {
                hdlr!!.removeCallbacks(this)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        hdlr!!.removeCallbacks(UpdateSongTime)
    }
}