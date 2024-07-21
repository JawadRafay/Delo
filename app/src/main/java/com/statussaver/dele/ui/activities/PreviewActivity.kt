package com.statussaver.dele.ui.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import com.statussaver.dele.R
import com.statussaver.dele.adapters.PreviewAdapter
import com.statussaver.dele.database.PrefManager
import com.statussaver.dele.databinding.ActivityPreviewBinding
import com.statussaver.dele.model.StatusModel
import com.statussaver.dele.utils.Utils
import java.io.File
import java.net.URLConnection


class PreviewActivity : AppCompatActivity() {

    private var previewAdapter: PreviewAdapter? = null
    private var statusDownload: String? = null
    private var imageList: ArrayList<StatusModel>? = null
    var position = 0
    lateinit var prefManager: PrefManager
    private lateinit var binding: ActivityPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefManager = PrefManager(this)

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val nightModeFlags = applicationContext.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
            if (prefManager.getDarkMode())
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageList = intent.getParcelableArrayListExtra("images")
        position = intent.getIntExtra("position", 0)
        statusDownload = intent.getStringExtra("statusdownload")

        Log.d("PreviewImageList", "onCreate: " + imageList)

        if (statusDownload == "download") {
            binding.downloadIV.visibility = View.GONE
        } else {
            binding.downloadIV.visibility = View.VISIBLE
        }

        previewAdapter = PreviewAdapter(this@PreviewActivity, imageList!!)
        binding.viewPager.adapter = previewAdapter
        binding.viewPager.currentItem = position

        binding.backIV.setOnClickListener{
            finish()
        }

        binding.downloadIV.setOnClickListener {
            if (imageList!!.size > 0) {
                try {
                    Utils.download(
                        this@PreviewActivity,
                        imageList!![binding.viewPager.currentItem].filepath
                    )
                    Toast.makeText(
                        this@PreviewActivity,
                        resources.getString(R.string.saved_success),
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@PreviewActivity,
                        "Sorry we can't move file.try with other file.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                finish()
            }
        }

        val isFromWhatsStatus = intent.getBooleanExtra("isFromWhatsStatus", false)
        if (isFromWhatsStatus){
            binding.shareIV.visibility = View.INVISIBLE
        }

        binding.shareIV.setOnClickListener {
            if (imageList!!.size > 0) {
                if (isImageFile(imageList!![binding.viewPager.currentItem].filepath)) {
                    val imageFileToShare: File =
                        File(imageList!![binding.viewPager.currentItem].filepath)
                    val share = Intent(Intent.ACTION_SEND)
                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    share.type = "image/*"
                    val photoURI = FileProvider.getUriForFile(
                        applicationContext, applicationContext
                            .packageName + ".provider", imageFileToShare
                    )
                    share.putExtra(
                        Intent.EXTRA_STREAM,
                        photoURI
                    )
                    startActivity(Intent.createChooser(share, "Share via"))
                } else if (isVideoFile(imageList!![binding.viewPager.currentItem].filepath)) {
                    val videoURI = FileProvider.getUriForFile(
                        applicationContext,
                        applicationContext
                            .packageName + ".provider",
                        File(imageList!![binding.viewPager.currentItem].filepath!!)
                    )
                    val videoshare = Intent(Intent.ACTION_SEND)
                    videoshare.type = "*/*"
                    videoshare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    videoshare.putExtra(Intent.EXTRA_STREAM, videoURI)
                    startActivity(videoshare)
                }
            } else {
                finish()
            }
        }

        binding.deleteIV.setOnClickListener {
            if (imageList!!.size > 0) {
                val alertDialog = AlertDialog.Builder(this@PreviewActivity)
                alertDialog.setTitle(R.string.confirm)
                alertDialog.setMessage(R.string.del_status)
                alertDialog.setPositiveButton(
                    resources.getString(R.string.yes)
                ) { dialog, which ->
                    dialog.dismiss()
                    var currentItem = 0
                    val file = File(imageList!![binding.viewPager.currentItem].filepath!!)
                    if (file.exists()) {
                        if (imageList!!.size > 0 && binding.viewPager.currentItem < imageList!!.size) {
                            currentItem = binding.viewPager.currentItem
                        }
                        imageList!!.removeAt(binding.viewPager.currentItem)
                        previewAdapter = PreviewAdapter(this@PreviewActivity, imageList!!)
                        binding.viewPager.adapter = previewAdapter
                        val intent = Intent()
                        setResult(10, intent)
                        if (imageList!!.size > 0) {
                            binding.viewPager.currentItem = currentItem
                        } else {
                            finish()
                        }
                    }
                }
                alertDialog.setNegativeButton(
                    resources.getString(R.string.no)
                ) { dialogInterface, i -> dialogInterface.dismiss() }
                alertDialog.show()
            } else {
                finish()
            }
        }

    }

    private fun isImageFile(path: String?): Boolean {
        Log.d("isImagePAth", "isImageFile: "+ path)

        val mimeType = URLConnection.guessContentTypeFromName(path)
        return mimeType != null && mimeType.startsWith("image")
    }

    private fun isVideoFile(path: String?): Boolean {
        val mimeType = URLConnection.guessContentTypeFromName(path)
        return mimeType != null && mimeType.startsWith("video")
    }
}