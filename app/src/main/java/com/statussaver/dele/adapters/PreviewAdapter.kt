package com.statussaver.dele.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.net.toUri
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.statussaver.dele.R
import com.statussaver.dele.databinding.PreviewListItemBinding
import com.statussaver.dele.model.StatusModel
import com.statussaver.dele.ui.activities.VideoPlayerActivity
import com.statussaver.dele.utils.Utils
import java.io.File
import java.net.URLConnection

class PreviewAdapter(val activity : Activity, val data : ArrayList<StatusModel>) : PagerAdapter()
{
    var file : File? = null
    private lateinit var binding: PreviewListItemBinding
    override fun instantiateItem(container: ViewGroup, position: Int): Any
    {

        binding = PreviewListItemBinding.inflate(LayoutInflater.from(activity))
       // val itemView: View = LayoutInflater.from(activity).inflate(R.layout.preview_list_item, container, false)
        Log.d("PreAdapterFile", "data: "+ data)
        file =  data[position].filepath?.let { File(it) }
        Log.d("PreAdapterFile", "instantiateItemFileWithPosition: "+ file)
        /*if (!file!!.isDirectory) {*/
            if (
                isVideoFile(file.toString())
            ) {
                try {
                    binding.iconplayer.visibility = View.VISIBLE
                    Glide.with(activity.applicationContext).load(data[position].filepath).into(binding.imageView)
                    Log.d("PreAdapterFile", "instantiateItemVideo: " + file)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("PreAdapterFile", "catch: " + file)
                }
            } else {
                try {
                    binding.iconplayer.visibility = View.GONE
                    Log.d("PreAdapterFile", "instantiateItemImage: " + file)
                    Glide.with(activity.applicationContext).load(data[position].filepath).into(binding.imageView)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        //}
        binding.imageView.setOnClickListener {
            if (Utils.getBack(
                    data[position].filepath,
                    "((\\.mp4|\\.webm|\\.ogg|\\.mpK|\\.avi|\\.mkv|\\.flv|\\.mpg|\\.wmv|\\.vob|\\.ogv|\\.mov|\\.qt|\\.rm|\\.rmvb\\.|\\.asf|\\.m4p|\\.m4v|\\.mp2|\\.mpeg|\\.mpe|\\.mpv|\\.m2v|\\.3gp|\\.f4p|\\.f4a|\\.f4b|\\.f4v)$)"
                )!!.isNotEmpty()
            ) {
                Utils.mPath = data[position].filepath
                activity.startActivity(Intent(activity, VideoPlayerActivity::class.java))
            }
        }
        container.addView(binding.root)
        return binding.root
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as RelativeLayout
    }

    private fun isVideoFile(path: String?): Boolean {
        val mimeType = URLConnection.guessContentTypeFromName(path)
        return mimeType != null && mimeType.startsWith("video")
    }
}