package com.statussaver.dele.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.statussaver.dele.R
import com.statussaver.dele.databinding.RowDocumentBinding
import com.statussaver.dele.model.AudioModel
import java.io.File
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*

class DocAdapter(val data : List<AudioModel>, val context : Context) : RecyclerView.Adapter<DocAdapter.MyItemViewHolder>() {

    private val TAG = "DocAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemViewHolder {
        return MyItemViewHolder(RowDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MyItemViewHolder, position: Int) {
        val model = data[position]
        holder.binding.tvName.text = model.title
        holder.binding.tvDate.text = convertTime(model.dates)
        holder.itemView.setOnClickListener {
            openFile(model.file)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class MyItemViewHolder(val binding : RowDocumentBinding) : RecyclerView.ViewHolder(binding.root)

    private fun convertTime(time: Long): String? {
        val date = Date(time)
        val format: Format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(date)
    }

    fun openFile(url: File) {
        Log.d(TAG, "openFile: "+url.name)
        Log.d(TAG, "openFile: "+url.absoluteFile)
        Log.d(TAG, "openFile: $url")
        val uri = Uri.fromFile(url)
        val intent = Intent(Intent.ACTION_VIEW)
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword")
        } else if (url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf")
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel")
        } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav")
        } else if (url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf")
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav")
        } else if (url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif")
        } else if (url.toString().contains(".jpg") || url.toString()
                .contains(".jpeg") || url.toString().contains(".png")
        ) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg")
        } else if (url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain")
        } else if (url.toString().contains(".3gp") || url.toString()
                .contains(".mpg") || url.toString().contains(".mpeg") || url.toString()
                .contains(".mpe") || url.toString().contains(".mp4") || url.toString()
                .contains(".avi")
        ) {
            // Video files
            intent.setDataAndType(uri, "video/*")
        } else {
            intent.setDataAndType(uri, "*/*")
        }

        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "openFile: ", e)
            Toast.makeText(context, "Invalid file format!", Toast.LENGTH_SHORT)
                .show()
        }
    }
}