package com.statussaver.dele.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.statussaver.dele.R
import com.statussaver.dele.adapters.VoiceAdapter
import com.statussaver.dele.databinding.FragmentMyVoicesBinding
import com.statussaver.dele.model.AudioModel
import com.statussaver.dele.services.DataService
class MyVoices : Fragment() {

    var activity: Activity? = null
    private lateinit var binding: FragmentMyVoicesBinding
    private lateinit var emptyLay: RelativeLayout

    private var modelArrayList = ArrayList<AudioModel>()

    private val REQUEST_STORAGE_PERMISSION = 123
    private val PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyVoicesBinding.inflate(layoutInflater,container,false)
        emptyLay = binding.root.findViewById(R.id.emptyLayout)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.voiceRecycler.layoutManager = LinearLayoutManager(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){

            if (arePermissionDenied()) {
                // If Android 10+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    requestPermissions(PERMISSIONS, REQUEST_STORAGE_PERMISSION)
                    return
                }
            } else {
                accessFiles()
            }
        } else {
            DataService.whatsapp_voices.observe(viewLifecycleOwner, Observer {
                if (it.isEmpty()) {
                    emptyLay.visibility = View.VISIBLE
                } else {
                    emptyLay.visibility = View.GONE
                    activity?.let { context ->
                        val adp = VoiceAdapter(it, context);
                        binding.voiceRecycler.adapter = adp
                    }

                }
            })
        }

    }
//Permissions
    private fun arePermissionDenied(): Boolean {
        for (permissions in PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(requireContext(), permissions) != PackageManager.PERMISSION_GRANTED) {
                return true
            }
        }
        return false
    }

    private fun accessFiles() {
        // Define the folder path
        val folderPath = "/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes"

        val contentResolver = requireContext().contentResolver
        val projection = arrayOf(
            MediaStore.Audio.Media._ID, // Include file ID in projection
            MediaStore.Audio.Media.DATA
        )
        val selection = "${MediaStore.Audio.Media.DATA} LIKE '$folderPath%'"
        // Change this to match your directory
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.DATE_TAKEN} DESC"
        )

        cursor?.use { cursor ->
            //statusModelsList = mutableListOf()
            while (cursor.moveToNext()) {
                val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                modelArrayList.add(AudioModel(title, path))
                Log.d("sUri", "accessFiles: " + modelArrayList)
            }
        }

        if (modelArrayList.isEmpty()) {
            emptyLay.visibility = View.VISIBLE
        } else {
            emptyLay.visibility = View.GONE
        }

        // Initialize and set up the adapter with the retrieved StatusModel objects
        activity?.let { context ->
            val adp = VoiceAdapter(modelArrayList, context);
            binding.voiceRecycler.adapter = adp
        }
    }

}