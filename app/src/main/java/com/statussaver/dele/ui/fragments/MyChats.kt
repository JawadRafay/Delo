package com.statussaver.dele.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.statussaver.dele.R
import com.statussaver.dele.adapters.ChatsAdapter
import com.statussaver.dele.databinding.FragmentMyChatsBinding
import com.statussaver.dele.model.ContactModel
import com.statussaver.dele.services.NotificationService
import com.statussaver.dele.utils.RefreshListener
import com.statussaver.dele.utils.SqliteHelper
import java.lang.String
import java.util.*
import kotlin.Comparator
import kotlin.Int
import kotlin.let

class MyChats : Fragment(), RefreshListener {

    private var sqliteHelper: SqliteHelper? = null
    var adapter: ChatsAdapter? = null
    private lateinit var binding: FragmentMyChatsBinding
    private lateinit var emptyLay: RelativeLayout

    companion object{
        var onNewMessage: onNewMessage? = null
        fun setListener(onMessage: onNewMessage?) {
            onNewMessage = onMessage
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyChatsBinding.inflate(layoutInflater,container,false)
        emptyLay = binding.root.findViewById(R.id.emptyLayout)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        NotificationService().setListener(this)
        fetchList()
    }

    fun init(){
        context?.let {
            sqliteHelper = SqliteHelper(it)
        }
        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        layoutManager.isSmoothScrollbarEnabled = true
        layoutManager.reverseLayout = true
        binding.rvChats!!.layoutManager = layoutManager
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchList() {
        var list: List<ContactModel> = ArrayList()
        list = sqliteHelper!!.getAllData()
        Collections.sort(list, CustomComparator())
        adapter = context?.let { ChatsAdapter(list , it) }
        adapter?.notifyDataSetChanged()
        adapter?.let {
            binding.rvChats.adapter = it
            if (list.isEmpty())
                emptyLay.visibility = View.VISIBLE
            else
                emptyLay.visibility = View.GONE
        }


    }

    override fun onRefresh(model: ContactModel?) {
        fetchList()
        Log.d("TAG", "onRefresh: ")
        if (onNewMessage != null)
            onNewMessage?.onMessageReceived(model)
    }

    interface onNewMessage {
        fun onMessageReceived(bean: ContactModel?)
    }

    class CustomComparator : Comparator<ContactModel?> {
        // may be it would be Model
        override fun compare(o1: ContactModel?, o2: ContactModel?): Int {
            return String.valueOf(o1?.time)
                .compareTo(String.valueOf(o2?.time))
        }
    }

    override fun onResume() {
        super.onResume()
        fetchList()
    }
}