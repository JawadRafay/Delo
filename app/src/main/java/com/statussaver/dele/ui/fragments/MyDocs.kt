package com.statussaver.dele.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.statussaver.dele.R
import com.statussaver.dele.adapters.DocAdapter
import com.statussaver.dele.adapters.PhotoAdapter
import com.statussaver.dele.databinding.FragmentMyDocBinding
import com.statussaver.dele.services.DataService
import com.statussaver.dele.services.DataServiceAboveTenVersion

class MyDocs : Fragment() {

    var activity: Activity? = null
    private lateinit var binding: FragmentMyDocBinding
    private lateinit var emptyLay: RelativeLayout

    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyDocBinding.inflate(layoutInflater,container,false)
        emptyLay = binding.root.findViewById(R.id.emptyLayout)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = binding.swipeRefreshLayout

        binding.rvDoc.layoutManager = GridLayoutManager(context,3)

        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            swipeRefreshLayout!!.setColorSchemeColors(
                ContextCompat.getColor(requireActivity(), android.R.color.holo_orange_dark),
                ContextCompat.getColor(requireActivity(), android.R.color.holo_green_dark),
                ContextCompat.getColor(requireActivity(), R.color.purple_200),
                ContextCompat.getColor(requireActivity(), android.R.color.holo_blue_dark)
            )

            swipeRefreshLayout!!.setOnRefreshListener {
                startServices()
                loadWhatsappDocumentAbove10()}

            loadWhatsappDocumentAbove10()
        } else {

            DataService.whatsapp_docs.observe(viewLifecycleOwner, Observer {
                if (it.isEmpty()) {
                    emptyLay.visibility = View.VISIBLE
                } else {
                    emptyLay.visibility = View.GONE
                    activity?.let { context ->
                        val adp = DocAdapter(it, context);
                        binding.rvDoc.adapter = adp
                    }

                }
            })
        }

    }

    private fun startServices() {
        val context = requireContext().applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, Intent(context, DataService::class.java))        } else {
            context.startService(Intent(context, DataService::class.java))        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadWhatsappDocumentAbove10(){
        DataServiceAboveTenVersion.whatsAppDocument.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                emptyLay.visibility = View.VISIBLE
            } else {
                emptyLay.visibility = View.GONE
                activity?.let { context ->
                    val adp = DocAdapter(it, context);
                    binding.rvDoc.adapter = adp

                    adp.notifyDataSetChanged()
                }

            }
        })

        swipeRefreshLayout!!.isRefreshing = false
    }

}