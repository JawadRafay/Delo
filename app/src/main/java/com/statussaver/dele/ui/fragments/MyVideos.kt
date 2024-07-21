package com.statussaver.dele.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.statussaver.dele.R
import com.statussaver.dele.adapters.VideoAdapter
import com.statussaver.dele.databinding.FragmentMyStatusBinding
import com.statussaver.dele.model.StatusModel
import com.statussaver.dele.services.DataService
import com.statussaver.dele.services.DataServiceAboveTenVersion
import com.statussaver.dele.utils.Utils
import java.io.File

class MyVideos : Fragment(), VideoAdapter.OnCheckboxListener {


    private val TAG = "MyVideos"
    var myAdapter: VideoAdapter? = null
    var filesToDelete = ArrayList<StatusModel>()
    var modelArrayList = ArrayList<StatusModel>()
    private lateinit var binding: FragmentMyStatusBinding
    private lateinit var emptyLay: RelativeLayout

    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    private val REQUEST_CODE_PERMISSION_FOR_IMAGES = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyStatusBinding.inflate(layoutInflater, container, false)
        emptyLay = binding.root.findViewById(R.id.emptyLayout)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            swipeRefreshLayout!!.setColorSchemeColors(
                ContextCompat.getColor(requireActivity(), android.R.color.holo_orange_dark),
                ContextCompat.getColor(requireActivity(), android.R.color.holo_green_dark),
                ContextCompat.getColor(requireActivity(), R.color.purple_200),
                ContextCompat.getColor(requireActivity(), android.R.color.holo_blue_dark)
            )
            swipeRefreshLayout!!.setOnRefreshListener {
                startServices()
                loadWhatsappVideosAbove10()
            }
            loadWhatsappVideosAbove10()
        } else {
            DataService.whatsapp_videos.observe(viewLifecycleOwner, Observer {

                if (it.isEmpty()) {
                    emptyLay.visibility = View.VISIBLE
                } else {
                    emptyLay.visibility = View.GONE
                }
                modelArrayList.clear()
                modelArrayList.addAll(it)
                myAdapter = context?.let { it1 -> VideoAdapter(modelArrayList, it1) }
                binding.statusRecycler.adapter = myAdapter
                myAdapter?.setOnCheckChangeListener(this)
            })
        }

        binding.deleteIV.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                if (filesToDelete.isNotEmpty()) {
                    AlertDialog.Builder(context)
                        .setMessage(resources.getString(R.string.delete_alert))
                        .setCancelable(true)
                        .setNegativeButton(
                            resources.getString(R.string.yes)
                        ) { _: DialogInterface?, _: Int ->

                            try {
                                deleteVideoFiles(filesToDelete)
                            } catch (recoverableSecurityException: RecoverableSecurityException) {
                                val intentSender =
                                    recoverableSecurityException.userAction.actionIntent.intentSender
                                startIntentSenderForResult(
                                    intentSender, REQUEST_CODE_PERMISSION_FOR_IMAGES,
                                    null, 0, 0, 0, null
                                )
                            }

                        }
                        .setPositiveButton(
                            resources.getString(R.string.no)
                        ) { dialogInterface, i -> dialogInterface.dismiss() }.create().show()
                }
            } else {
                if (filesToDelete.isNotEmpty()) {
                    AlertDialog.Builder(context)
                        .setMessage(resources.getString(R.string.delete_alert))
                        .setCancelable(true)
                        .setNegativeButton(
                            resources.getString(R.string.yes)
                        ) { _: DialogInterface?, _: Int ->
                            var success = -1
                            val deletedFiles =
                                java.util.ArrayList<StatusModel>()
                            for (details in filesToDelete) {
                                val file = details.filepath?.let { File(it) }
                                success = if (file!!.exists()) {
                                    if (file.delete()) {
                                        deletedFiles.add(details)
                                        if (success == 0) {
                                            return@setNegativeButton
                                        }
                                        1
                                    } else {
                                        0
                                    }
                                } else {
                                    0
                                }
                            }
                            filesToDelete.clear()
                            for (deletedFile in deletedFiles) {
                                modelArrayList.remove(deletedFile)
                                DataService.whatsapp_videos.value =
                                    DataService.whatsapp_videos.value!!.toMutableList().apply {
                                        remove(deletedFile)
                                    }.toList()
                            }
                            myAdapter!!.notifyDataSetChanged()
                            if (success == 0) {
                                Toast.makeText(
                                    context,
                                    resources.getString(R.string.delete_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (success == 1) {
                                Toast.makeText(
                                    activity,
                                    resources.getString(R.string.delete_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            binding.actionLay.visibility = View.GONE
                            binding.selectAll.isChecked = false
                        }
                        .setPositiveButton(
                            resources.getString(R.string.no)
                        ) { dialogInterface, i -> dialogInterface.dismiss() }.create().show()
                }
            }
        }

        binding.downloadIV.setOnClickListener {
            if (filesToDelete.isNotEmpty()) {
                var success = -1
                val downloadedFiles = ArrayList<StatusModel>()
                for (details in filesToDelete) {

                    val file = details.filepath?.let { File(it) }
                    success = if (file!!.exists()) {
                        if (Utils.download(requireContext(), details.filepath)) {
                            downloadedFiles.add(details)
                            if (success == 0) {
                                return@setOnClickListener
                            }
                            1
                        } else {
                            0
                        }
                    } else {
                        0
                    }
                }
                filesToDelete.clear()
                for (downloadedFile in downloadedFiles) {
                    val index = modelArrayList.indexOf(downloadedFile);
                    modelArrayList[index].selected = false
                }
                myAdapter!!.notifyDataSetChanged()
                if (success == 0) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.save_error),
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (success == 1) {
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.save_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.actionLay.visibility = View.GONE
                binding.selectAll.isChecked = false
            }
        }

        binding.selectAll.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (!compoundButton.isPressed) {
                return@setOnCheckedChangeListener
            }
            filesToDelete.clear()
            if (b) {
                for (i in modelArrayList.indices) {
                    modelArrayList[i].selected = true
                    filesToDelete.add(modelArrayList[i])
                }
                binding.selectAll.isChecked = true
            } else {
                for (i in modelArrayList.indices) {
                    modelArrayList[i].selected = false
                }
                binding.actionLay.visibility = View.GONE
            }
            myAdapter!!.notifyDataSetChanged()
        }
    }

    private fun startServices() {
        val context = requireContext().applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, Intent(context, DataService::class.java))
        } else {
            context.startService(Intent(context, DataService::class.java))
        }
    }

    private fun loadWhatsappVideosAbove10() {
        DataServiceAboveTenVersion.whatsAppVideos.observe(viewLifecycleOwner, Observer {

            Log.d("uriPAt", "onViewCreated: " + modelArrayList)

            if (it.isEmpty()) {
                emptyLay.visibility = View.VISIBLE
            } else {
                emptyLay.visibility = View.GONE
            }
            modelArrayList.clear()
            modelArrayList.addAll(it)
            myAdapter = context?.let { it1 -> VideoAdapter(modelArrayList, it1) }
            binding.statusRecycler.adapter = myAdapter
            myAdapter?.setOnCheckChangeListener(this)
            swipeRefreshLayout!!.isRefreshing = false
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteVideoFiles(filesToDelete: ArrayList<StatusModel>) {
        val filesToDeleteCopy =
            ArrayList(filesToDelete) // Create a copy to avoid ConcurrentModificationException
        val contentResolver = requireContext().contentResolver

        val updatedList = ArrayList(modelArrayList)

        for (statusModel in filesToDeleteCopy) {
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                statusModel.id!!.toLong()
            )
            try {
                val deletedCount = contentResolver.delete(contentUri, null, null)
                if (deletedCount > 0) {
                    // Image deleted successfully
                    updatedList.remove(statusModel)
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.delete_success),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Image deletion failed
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.delete_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException = securityException as?
                            RecoverableSecurityException
                        ?: throw RuntimeException(securityException.message, securityException)
                    val intentSender =
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    intentSender?.let {
                        startIntentSenderForResult(
                            intentSender, REQUEST_CODE_PERMISSION_FOR_IMAGES,
                            null, 0, 0, 0, null
                        )
                    }
                } else {
                    throw RuntimeException(securityException.message, securityException)
                }
            }
        }
        // Remove items from filesToDelete list after the loop
//        filesToDelete.removeAll(filesToDeleteCopy.toSet())
        DataServiceAboveTenVersion.whatsAppVideos.postValue(updatedList)
        // Notify adapter of changes
        myAdapter?.notifyDataSetChanged()
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PERMISSION_FOR_IMAGES) {
            // Check if the user granted permission
            if (resultCode == Activity.RESULT_OK) {
                // User granted permission, retry the delete operation
                retryDeleteVideoOperation()
            } else {
                Toast.makeText(
                    activity,
                    resources.getString(R.string.Permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun retryDeleteVideoOperation() {
        val filesToDeleteCopy = ArrayList(filesToDelete)
        // Reattempt the delete operation for each selected video
        for (statusModel in filesToDeleteCopy) {
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                statusModel.id!!.toLong()
            )
            try {
                requireContext().contentResolver.delete(contentUri, null, null)
                // Image deleted successfully
                updatedList.remove(statusModel)
                Toast.makeText(
                    activity,
                    resources.getString(R.string.delete_success),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                filesToDelete.clear()
                // Handle deletion failure
                Log.d(TAG, "retryDeleteVideoOperation: ")
            }
        }
        //filesToDelete.clear()
        DataServiceAboveTenVersion.whatsAppVideos.postValue(updatedList)
        myAdapter?.notifyDataSetChanged()
    }*/

    fun init() {
        binding.statusRecycler.layoutManager = GridLayoutManager(context, 3)
        swipeRefreshLayout = binding.swipeRefreshLayout
    }

    override fun onCheckboxListener(view: View?, list: List<StatusModel?>?) {
        filesToDelete.clear()
        for (details in list!!) {
            if (details!!.selected) {
                filesToDelete.add(details)
            }
        }
        if (filesToDelete.size == myAdapter!!.itemCount) {
            binding.selectAll.isChecked = true
        }
        if (filesToDelete.isNotEmpty()) {
            binding.actionLay.visibility = View.VISIBLE
            return
        }

        binding.selectAll.isChecked = false
        binding.actionLay.visibility = View.GONE
    }
}