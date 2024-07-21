package com.statussaver.dele.adapters

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.statussaver.dele.databinding.RowMyStatusBinding
import com.statussaver.dele.model.StatusModel
import com.statussaver.dele.ui.activities.PreviewActivity
import com.statussaver.dele.utils.Utils
import java.net.URLConnection

class StatusAdapter(val data : List<StatusModel>, val context: Context) : RecyclerView.Adapter<StatusAdapter.MyViewHolder>() {

    private var onCheckboxListener: OnCheckboxListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(RowMyStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        Log.d("fileDoc", "onCreateViewHolder: " + data)
        val model = data[position]
        Log.d("fileDoc", "model.filepath: " + model.filepath)

        if (Utils.getBack(
                model.filepath,
                "((\\.mp4|\\.webm|\\.ogg|\\.mpK|\\.avi|\\.mkv|\\.flv|\\.mpg|\\.wmv|\\.vob|\\.ogv|\\.mov|\\.qt|\\.rm|\\.rmvb\\.|\\.asf|\\.m4p|\\.m4v|\\.mp2|\\.mpeg|\\.mpe|\\.mpv|\\.m2v|\\.3gp|\\.f4p|\\.f4a|\\.f4b|\\.f4v)$)"
            )!!.isNotEmpty()
        )
            holder.binding.play.visibility = View.VISIBLE
        else
            holder.binding.play.visibility = View.GONE

        Glide.with(context).load(model.filepath).override(200, 200)
            .into(holder.binding.gridImageVideo)

        Log.d("TAGStatus", "executeNewAdapterModel: " + model.filepath);

        holder.binding.checkbox.isChecked = model.selected

        holder.binding.checkbox.setOnCheckedChangeListener{ buttonView, isChecked ->
            data[holder.adapterPosition].selected = isChecked
            onCheckboxListener?.onCheckboxListener(buttonView, data)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PreviewActivity::class.java)
            intent.putParcelableArrayListExtra("images", data as ArrayList<out Parcelable?>?)
            intent.putExtra("position", position)
            intent.putExtra("statusdownload", "download")
            intent.putExtra("isFromWhatsStatus", true) // indicate it's from WhatsStatus fragment
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class MyViewHolder(val binding : RowMyStatusBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnCheckboxListener {
        fun onCheckboxListener(view: View?, list: List<StatusModel?>?)
    }

    fun setOnCheckChangeListener(onCheckboxListener: OnCheckboxListener){
        this.onCheckboxListener = onCheckboxListener
    }

    private fun isVideoFile(path: String?): Boolean {
        /*if (path.isNullOrEmpty()) {
            Log.d("PathOfImage", "Here isVideoFile: $path")
            return false
        }
        Log.d("PathOfImage", "here isNotVideoFile: $path")*/

        val mimeType = URLConnection.guessContentTypeFromName(path)
        return mimeType != null && mimeType.startsWith("video")
    }
}