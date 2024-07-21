package com.statussaver.dele.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.statussaver.dele.R
import com.statussaver.dele.database.PrefManager
import com.statussaver.dele.databinding.ChatListRowBinding
import com.statussaver.dele.databinding.ItemVoiceBinding
import com.statussaver.dele.model.ContactModel
import java.text.SimpleDateFormat
import java.util.*

class ChatListAdapter(val data : List<ContactModel>, val context: Context) : RecyclerView.Adapter<ChatListAdapter.MyViewHolder>() {


    var clipboard: ClipboardManager? = null
    lateinit var prefManager: PrefManager

    init {
        clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        prefManager = PrefManager(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ChatListRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val chat: ContactModel = data[position]

        holder.binding.tvMsg.text = chat.text
        holder.binding.tvMsgMe.text = chat.text
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.binding.tvMsg.text = Html.fromHtml(chat.text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            holder.binding.tvMsg.text = Html.fromHtml(chat.text)
        }
        Linkify.addLinks(holder.binding.tvMsg, Linkify.ALL)
        holder.binding.tvMsg.movementMethod = LinkMovementMethod.getInstance()

        val formatter = SimpleDateFormat("h:mm a")
        val dateString = formatter.format(Date(chat.time))
        holder.binding.tvTime.text = dateString
        holder.binding.tvTimeMe.text = dateString

        if (chat.type.equals("me")) {
            holder.binding.chatBox.visibility = View.GONE
            holder.binding.tvTime.visibility = View.GONE
            holder.binding.chatBoxMe.visibility = View.VISIBLE
            holder.binding.tvTimeMe.visibility = View.VISIBLE
        } else {
            holder.binding.chatBoxMe.visibility = View.GONE
            holder.binding.tvTimeMe.visibility = View.GONE
            holder.binding.chatBox.visibility = View.VISIBLE
            holder.binding.tvTime.visibility = View.VISIBLE
        }

        holder.binding.tvMsgMe.setOnLongClickListener(OnLongClickListener {
            val clip = ClipData.newPlainText("message", chat.text)
            clipboard!!.setPrimaryClip(clip)
            Toast.makeText(context, "text copied", Toast.LENGTH_SHORT).show()
            true
        })

        holder.binding.tvMsg.setOnLongClickListener(OnLongClickListener {
            val clip = ClipData.newPlainText("message", chat.text)
            clipboard!!.setPrimaryClip(clip)
            Toast.makeText(context, "text copied", Toast.LENGTH_SHORT).show()
            true
        })

        if (prefManager.getDarkMode()){
            holder.binding.chatBox.background = context.resources.getDrawable(R.drawable.chat_bg_night)
            holder.binding.tvMsg.setTextColor(Color.parseColor("#ffffff"))
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

    class MyViewHolder(val binding: ChatListRowBinding) : RecyclerView.ViewHolder(binding.root)
}