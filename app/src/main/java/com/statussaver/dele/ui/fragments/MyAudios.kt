package com.statussaver.dele.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.statussaver.dele.R
import com.statussaver.dele.adapters.AudioAdapter
import com.statussaver.dele.adapters.DocAdapter
import com.statussaver.dele.databinding.FragmentMyAudiosBinding
import com.statussaver.dele.services.DataService
import com.statussaver.dele.services.DataServiceAboveTenVersion

class MyAudios : Fragment() {

    var activity: Activity? = null
    private lateinit var binding: FragmentMyAudiosBinding
    private lateinit var emptyLay:RelativeLayout

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
        binding = FragmentMyAudiosBinding.inflate(layoutInflater,container,false)
        emptyLay = binding.root.findViewById(R.id.emptyLayout)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = binding.swipeRefreshLayout

        binding.audiosRecycler.layoutManager = LinearLayoutManager(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            swipeRefreshLayout!!.setColorSchemeColors(
                ContextCompat.getColor(requireActivity(), android.R.color.holo_orange_dark),
                ContextCompat.getColor(requireActivity(), android.R.color.holo_green_dark),
                ContextCompat.getColor(requireActivity(), R.color.purple_200),
                ContextCompat.getColor(requireActivity(), android.R.color.holo_blue_dark)
            )

            swipeRefreshLayout!!.setOnRefreshListener {
                startServices()
                loadWhatsappAudioAbove10()}

            loadWhatsappAudioAbove10()
        } else {

            DataService.whatsapp_audios.observe(viewLifecycleOwner, Observer {
                if (it.isEmpty()) {
                    emptyLay.visibility = View.VISIBLE
                } else {
                    emptyLay.visibility = View.GONE

                    activity?.let { context ->
                        val adp = AudioAdapter(it, context)
                        binding.audiosRecycler.adapter = adp

                        adp.notifyDataSetChanged()
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

    private fun loadWhatsappAudioAbove10(){
        DataServiceAboveTenVersion.whatsAppAudio.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                emptyLay.visibility = View.VISIBLE
            } else {
                emptyLay.visibility = View.GONE

                activity?.let { context ->
                    val adp = AudioAdapter(it, context);
                    binding.audiosRecycler.adapter = adp
                }

            }
        })

        swipeRefreshLayout!!.isRefreshing = false
    }

}