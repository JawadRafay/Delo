package com.statussaver.dele.ui.activities

import android.annotation.SuppressLint
import android.app.Notification
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.statussaver.dele.R
import com.statussaver.dele.adapters.ChatListAdapter
import com.statussaver.dele.database.PrefManager
import com.statussaver.dele.databinding.ActivityChatViewBinding
import com.statussaver.dele.model.ContactModel
import com.statussaver.dele.services.NotificationService
import com.statussaver.dele.ui.fragments.MyChats
import com.statussaver.dele.utils.SqliteHelper
import java.util.*
import kotlin.math.log

class ChatViewActivity : AppCompatActivity() {

    var sqliteHelper: SqliteHelper? = null
    var list: MutableList<ContactModel>? = null
    var cid = 0
    var notification: Notification? = null
    var adapter: ChatListAdapter? = null
    var name: String? = null
    var logo: String? = null
    var isVisible: Boolean? = false
    lateinit var prefManager: PrefManager
    private val TAG = "ChatViewActivity"
    private lateinit var binding: ActivityChatViewBinding

    @SuppressLint("NotifyDataSetChanged")
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

        binding = ActivityChatViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()



        val intent = intent
        if (intent != null)
        {
            cid = intent.getIntExtra("cid", 0)
            name = intent.getStringExtra("name")
            try {
                logo = intent.getStringExtra("logo")
                if (logo != null) binding.ivLogo.setImageURI(Uri.parse(logo))
            } catch (_: Exception) { }

            //search in list
            for (model in NotificationService.notificationModels!!) {
                if (model.name.equals(name)) {
                    notification = model.notification
                    binding.sendLayout.visibility = View.VISIBLE
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        binding.ivLogo.setImageIcon(model.icon)
                    break
                }
            }
            list = sqliteHelper!!.getData(cid)
            sqliteHelper!!.resetCount(cid, name!!)
            if (name != null) {
                binding.tvName.text = name
            } else binding.tvName.text = getString(R.string.app_name)
        }else Log.d(TAG, "empty: ")
        if (list!!.isNotEmpty()) {
            val layoutManager = LinearLayoutManager(applicationContext)
            binding.rvChat.layoutManager = layoutManager
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager.isSmoothScrollbarEnabled = true
            layoutManager.stackFromEnd = true
            adapter = ChatListAdapter(list!!, applicationContext)
            binding.rvChat.adapter = adapter
            binding. rvChat.layoutManager?.scrollToPosition(list!!.size - 1)
        }

        binding.backIV.setOnClickListener {
            finish()
        }

        binding.send.setOnClickListener {
            if (binding.message.text.toString().trim { it <= ' ' }.isEmpty()) {
                Log.d(TAG, "null: ")
                return@setOnClickListener
            }

            if (notification != null)
            {
                val chat = ContactModel()
                chat.text = binding.message.text.toString().trim { it <= ' ' }
                chat.time = System.currentTimeMillis()
                chat.type = "me"
                list?.add(chat)
                adapter!!.notifyDataSetChanged()

                sqliteHelper!!.addContactID(
                    ContactModel(
                        "",
                        name,
                        logo,
                        binding.message.text.toString().trim { it <= ' ' },
                        System.currentTimeMillis(),
                        "me"
                    )
                )
                binding.rvChat.layoutManager?.scrollToPosition(list!!.size - 1)
                val actions = notification!!.actions
                if (actions != null) {
                    for (act in actions) {
                        if (act != null && act.remoteInputs != null) {
                            if (act.title.toString().contains("Reply")) {
                                if (act.remoteInputs != null) {
                                    Log.d(
                                        TAG, "onClick:isSent " + sendNativeIntent(
                                            applicationContext,
                                            act,
                                            binding.message.text.toString().trim { it <= ' ' })
                                    )
                                    break
                                }
                            }
                        }
                    }
                }
                binding.message.setText("")
            }
        }

        MyChats.setListener(object : MyChats.onNewMessage {
            override fun onMessageReceived(chat: ContactModel?) {
                if (chat?.name.equals(name)) {
                    chat?.time = System.currentTimeMillis()
                    chat?.type = "other"
                    chat?.let { list!!.add(it) }
                    if (isVisible == true)
                        sqliteHelper!!.resetCount(cid, name!!)

                    adapter!!.notifyDataSetChanged()
                    binding.rvChat.layoutManager?.scrollToPosition(list!!.size - 1)
                }
            }
        })

    }

    fun init() {
        sqliteHelper = SqliteHelper(this)
        list = ArrayList<ContactModel>()
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    private fun sendNativeIntent(
        context: Context,
        action: Notification.Action,
        msg: String
    ): Boolean {
        for (rem in action.remoteInputs) {
            val intent = Intent()
            val bundle = Bundle()
            bundle.putCharSequence(rem.resultKey, msg)
            RemoteInput.addResultsToIntent(action.remoteInputs, intent, bundle)
            try {
                action.actionIntent.send(context, 0, intent)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                return false
            }
            return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        isVisible = true
    }

    override fun onPause() {
        super.onPause()
        isVisible = false
    }
}