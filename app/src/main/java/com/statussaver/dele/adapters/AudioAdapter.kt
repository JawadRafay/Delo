package com.statussaver.dele.adapters

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.statussaver.dele.R
import com.statussaver.dele.databinding.ItemVoiceBinding
import com.statussaver.dele.model.AudioModel
import com.statussaver.dele.ui.activities.AudioPlayerActivity
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*

class AudioAdapter(val imagesList : List<AudioModel>,val context : Context) : RecyclerView.Adapter<AudioAdapter.MyItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemViewHolder {

        return MyItemViewHolder(ItemVoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: AudioAdapter.MyItemViewHolder, position: Int) {

        val status = imagesList[position]
        holder.binding.tvFileName.text = status.title
        holder.binding.tvFileDate.text = convertTime(status.dates)

        holder.itemView.setOnClickListener {
            context.startActivity(
                Intent(context, AudioPlayerActivity::class.java)
                    .putParcelableArrayListExtra(
                        "imagesList",
                        imagesList as ArrayList<out Parcelable?>
                    )
                    .putExtra("position", position)
                    .putExtra("path", status.paths)
                    .putExtra("name", status.title)
            )
        }

    }

    override fun getItemCount(): Int {
        return imagesList.size
    }

    class MyItemViewHolder(val binding : ItemVoiceBinding) : RecyclerView.ViewHolder(binding.root)

    private fun convertTime(time: Long): String? {
        val date = Date(time)
        val format: Format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return format.format(date)
    }
}