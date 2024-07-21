package com.statussaver.dele.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
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
import com.statussaver.dele.adapters.PhotoAdapter
import com.statussaver.dele.databinding.FragmentMyDownVideosBinding
import com.statussaver.dele.model.StatusModel
import com.statussaver.dele.services.DataService
import java.io.File

class MyDownloadVideos : Fragment(), PhotoAdapter.OnCheckboxListener {

    private val TAG = "MyDownloadVideos"
    var myAdapter: PhotoAdapter? = null
    var filesToDelete = ArrayList<StatusModel>()
    var modelArrayList = ArrayList<StatusModel>()

    private val PERMISSION_REQUEST_CODE = 1001

    private lateinit var binding: FragmentMyDownVideosBinding
    private lateinit var emptyLay: RelativeLayout

    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyDownVideosBinding.inflate(layoutInflater,container,false)
        emptyLay = binding.root.findViewById(R.id.emptyLayout)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        swipeRefreshLayout!!.setColorSchemeColors(
            ContextCompat.getColor(requireActivity(), android.R.color.holo_orange_dark),
            ContextCompat.getColor(requireActivity(), android.R.color.holo_green_dark),
            ContextCompat.getColor(requireActivity(), R.color.purple_200),
            ContextCompat.getColor(requireActivity(), android.R.color.holo_blue_dark)
        )

        swipeRefreshLayout!!.setOnRefreshListener {
            startServices()
            loadWhatsappVideosAbove10()}

        loadWhatsappVideosAbove10()

        binding.deleteIV.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                deleteSelectedVideos()
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
                                DataService.whatsapp_status_download_videos.value =
                                    DataService.whatsapp_status_download_photos.value!!.toMutableList()
                                        .apply {
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
            ContextCompat.startForegroundService(context, Intent(context, DataService::class.java))        } else {
            context.startService(Intent(context, DataService::class.java))        }
    }

    private fun loadWhatsappVideosAbove10(){
        DataService.whatsapp_status_download_videos.observe(viewLifecycleOwner, Observer {

            if (it.isEmpty()) {
                emptyLay.visibility = View.VISIBLE
            } else {
                emptyLay.visibility = View.GONE
            }
            modelArrayList.clear()
            modelArrayList.addAll(it)
            myAdapter = context?.let { it1 -> PhotoAdapter(modelArrayList, it1) }
            binding.downloadVideos.adapter = myAdapter
            myAdapter?.setOnCheckChangeListener(this)

            swipeRefreshLayout!!.isRefreshing = false
        })
    }

    private fun deleteSelectedVideos() {
        val permissionsNeeded = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissionsNeeded.add(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        }
        if (permissionsNeeded.isNotEmpty()) {
            requestPermissions(permissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            performDeleteAction()
        }
    }

    private fun performDeleteAction() {
        if (filesToDelete.isNotEmpty()) {
            AlertDialog.Builder(context)
                .setMessage(resources.getString(R.string.delete_alert))
                .setCancelable(true)
                .setNegativeButton(
                    resources.getString(R.string.yes)
                ) { _: DialogInterface?, _: Int ->
                    deleteDownloadVideos()
                }
                .setPositiveButton(
                    resources.getString(R.string.no)
                ) { dialogInterface, i -> dialogInterface.dismiss() }.create().show()
        }
    }

    private fun deleteDownloadVideos() {
        for (fileToDelete in filesToDelete) {
            val file = File(fileToDelete.filepath.toString())
            if (file.exists() && file.isFile) {
                if (file.delete()) {
                    // Remove the deleted file from the list and LiveData
                    modelArrayList.remove(fileToDelete)
                    DataService.whatsapp_status_download_photos.value = DataService.whatsapp_status_download_photos.value?.toMutableList()?.apply {
                        remove(fileToDelete)
                    }?.toList()
                    Log.d(TAG, "Deleted file: ${file.absolutePath}")
                } else {
                    Log.e(TAG, "Failed to delete file: ${file.absolutePath}")
                }
            }
        }
        // Clear the files to delete list
        filesToDelete.clear()
        // Notify adapter of the changes
        myAdapter?.notifyDataSetChanged()
        // Hide action layout
        binding.actionLay.visibility = View.GONE
        // Uncheck select all checkbox
        binding.selectAll.isChecked = false
    }

    private fun deleteFilesInDirectory(directory: File) {
        if (directory.isDirectory) {
            val listFiles = directory.listFiles()
            listFiles?.forEach { file ->
                if (file.isFile) {
                    if (file.delete()) {
                        Toast.makeText(activity, resources.getString(R.string.delete_success) + file.absolutePath, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Deleted file: ${file.absolutePath}")
                    } else {
                        Toast.makeText(context, resources.getString(R.string.delete_error) + file.absolutePath, Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Failed to delete file: ${file.absolutePath}")
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                // Permission denied by the user, show a toast or dialog informing the user about the permission.
                Toast.makeText(context, "Permission denied by the user", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onRequestPermissionsResult: Permission denied by the user")

                // Guide the user to grant the permission manually
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.fromParts("package", requireContext().packageName, null)
                startActivity(intent)
            } else {
                // Permission granted or targeting a version below Android 11
                performDeleteAction()
            }
        }
    }

    fun init(){
        binding.downloadVideos.layoutManager = GridLayoutManager(context, 3)
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