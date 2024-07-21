package com.statussaver.dele.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.statussaver.dele.R
import com.statussaver.dele.databinding.ChatsRowBinding
import com.statussaver.dele.model.ContactModel
import com.statussaver.dele.services.NotificationService
import com.statussaver.dele.ui.activities.ChatViewActivity
import com.statussaver.dele.utils.SqliteHelper
import java.text.SimpleDateFormat
import java.util.*

class ChatsAdapter(val data : List<ContactModel>, val context: Context) : RecyclerView.Adapter<ChatsAdapter.MyViewHolder>()  {

    private var sqliteHelper: SqliteHelper? = null
    init {
        sqliteHelper = SqliteHelper(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ChatsRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val model: ContactModel = data[position]

        holder.binding.tvName.setText(model.name)


        holder.binding.tvMsg.setText(model.text)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NotificationService.notificationModels.let {
                    if (it != null) {
                        for (notification in it) {
                            if (notification.name.equals(model.name)) {
                                holder.binding.ivLogo.setImageIcon(notification.icon)
                                break
                            }
                        }
                    }
                }
            }else holder.binding.ivLogo.setImageURI(
                Uri.parse(model.logo)
            )
        } catch (e: Exception) {
            holder.binding.ivLogo.setImageDrawable(context.resources.getDrawable(R.drawable.profile))
        }

        val id = sqliteHelper!!.getCountsDataByID(model.id)
        if (id == 0) {
            holder.binding.rlCounter.visibility = View.INVISIBLE
        } else {
            holder.binding.rlCounter.visibility = View.VISIBLE
            holder.binding.tvCounter.text = id.toString()
        }

        val formatter = SimpleDateFormat("h:mm a")
        val dateFormatter = SimpleDateFormat("dd-MM-yyyy")
        val dateString = formatter.format(Date(model.time))
        if (getDate() == dateFormatter.format(Date(model.time)))
            holder.binding.tvTime.text = dateString
        else {
            holder.binding.tvTime.text = dateFormatter.format(Date(model.time))
        }


        holder.binding.layout.setOnClickListener(View.OnClickListener {
            context
                .startActivity(
                    Intent(context, ChatViewActivity::class.java)
                        .putExtra("cid", model.id)
                        .putExtra("name", model.name)
                        .putExtra("logo", model.logo)
                )
        })

    }

    override fun getItemCount(): Int {
        return data.size
    }

    class MyViewHolder(val binding : ChatsRowBinding) : RecyclerView.ViewHolder(binding.root)

    private fun getDate(): String? {
        val dateFormatter = SimpleDateFormat("dd-MM-yyyy")
        return dateFormatter.format(Date(Calendar.getInstance().timeInMillis))
    }
}