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

class PhotoAdapter(val data : List<StatusModel>, val context: Context) : RecyclerView.Adapter<PhotoAdapter.MyViewHolder>() {

    private var onCheckboxListener: OnCheckboxListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(RowMyStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d("fileDoc", "photoData: " + data)
        val model = data[position]

        Log.d("fileDoc", "photoModel.filepath: "+model.filepath)

        if (Utils.getBack(
                model.filepath,
                "((\\.jpg|\\.png|\\.gif|\\.jpeg|\\.bmp)$)"
            )!!.isNotEmpty()
        )
            holder.binding.play.visibility = View.GONE
        else
            holder.binding.play.visibility = View.VISIBLE

        Glide.with(context).load(model.filepath).override(200, 200)
            .into(holder.binding.gridImageVideo)
        Log.d("FileModel", "onBindViewHolder: "+model.filepath)

        holder.binding.checkbox.isChecked = model.selected

        holder.binding.checkbox.setOnCheckedChangeListener{ buttonView, isChecked ->
            data[holder.adapterPosition].selected = isChecked
            onCheckboxListener?.let {
                it.onCheckboxListener(buttonView,data)
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PreviewActivity::class.java)
            intent.putParcelableArrayListExtra("images", data as ArrayList<out Parcelable?>?)
            intent.putExtra("position", position)
            intent.putExtra("statusdownload", "download")
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
        val mimeType = URLConnection.guessContentTypeFromName(path)
        return mimeType != null && mimeType.startsWith("video")
    }
}