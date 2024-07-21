package com.statussaver.dele.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.storage.StorageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.statussaver.dele.R
import com.statussaver.dele.adapters.PhotoAdapter
import com.statussaver.dele.adapters.StatusAdapter
import com.statussaver.dele.databinding.FragmentMyStatusBinding
import com.statussaver.dele.model.StatusModel
import com.statussaver.dele.services.DataService
import com.statussaver.dele.utils.Common
import com.statussaver.dele.services.DataServiceAboveTenVersion
import com.statussaver.dele.utils.Utils
import java.io.File

class WhatsappStatus : Fragment(), StatusAdapter.OnCheckboxListener {

    private var myAdapter: StatusAdapter? = null
    private var filesToDelete = ArrayList<StatusModel>()
    private var statusModelArrayList = ArrayList<StatusModel>()
    private lateinit var dataService: DataService
    private lateinit var binding: FragmentMyStatusBinding
    private lateinit var emptyLay: RelativeLayout

    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyStatusBinding.inflate(layoutInflater, container, false)
        emptyLay = binding.root.findViewById(R.id.emptyLayout)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        init()

        //Permission
        if (arePermissionDenied()) {
            // If Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requestPermissionQ()
                return
            }
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS)
        }
        //--------------------------------------------------------------------

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            swipeRefreshLayout!!.setColorSchemeColors(
                ContextCompat.getColor(requireActivity(), android.R.color.holo_orange_dark),
                ContextCompat.getColor(requireActivity(), android.R.color.holo_green_dark),
                ContextCompat.getColor(requireActivity(), R.color.purple_200),
                ContextCompat.getColor(requireActivity(), android.R.color.holo_blue_dark)
            )

            loadWhatsappStatusesAbove10()
            swipeRefreshLayout!!.setOnRefreshListener {
                startServices()
                loadWhatsappStatusesAbove10()
            }

        } else {
            DataService.whatsapp_status.observe(viewLifecycleOwner, Observer {

                if (it.isEmpty()) {
                    emptyLay.visibility = View.VISIBLE
                } else {
                    emptyLay.visibility = View.GONE
                }

                Log.d("check_tag", "CheckVersion10: " + it)

                statusModelArrayList.clear()
                statusModelArrayList.addAll(it)
                myAdapter = context?.let { it1 -> StatusAdapter(statusModelArrayList, it1) }
                binding.statusRecycler.adapter = myAdapter
                myAdapter?.setOnCheckChangeListener(this)
            })
        }

        binding.deleteIV.setOnClickListener {
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
                                val delFile = File(details.filepath.toString())
                                delFile.delete()

                                //statusModelArrayList.remove(details)
                                myAdapter?.notifyDataSetChanged()

                                Toast.makeText(context, "File Deleted", Toast.LENGTH_SHORT).show()

                            }
                            binding.actionLay.visibility = View.GONE
                            binding.selectAll.isChecked = false
                        }
                        .setPositiveButton(
                            resources.getString(R.string.no)
                        ) { dialogInterface, i -> dialogInterface.dismiss() }.create().show()
                }
            } else {*/
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
                                statusModelArrayList.remove(deletedFile)
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
            //}
        }

        binding.downloadIV.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (filesToDelete.isNotEmpty()) {
                    for (status in filesToDelete) {
                        val isSaved = Common.copyFile(status, requireContext(), binding.root)
                        if (isSaved) {
                            // Display success message
                            Toast.makeText(
                                activity,
                                resources.getString(R.string.save_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Display error message
                            Toast.makeText(
                                context,
                                resources.getString(R.string.save_error),
                                Toast.LENGTH_SHORT
                            ).show()                        }
                    }
                    // Clear the list after copying all files
                    filesToDelete.clear()
                    // Notify user or perform any other action if needed
                }
            } else {
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
                        val index = statusModelArrayList.indexOf(downloadedFile);
                        statusModelArrayList[index].selected = false
                    }
                    myAdapter?.notifyDataSetChanged()
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        activity?.startForegroundService(Intent(activity, DataService::class.java))
                    } else {
                        activity?.startService(Intent(activity, DataService::class.java))
                    }
                }
            }
        }

        binding.selectAll.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (!compoundButton.isPressed) {
                return@setOnCheckedChangeListener
            }
            filesToDelete.clear()
            if (b) {
                for (i in statusModelArrayList.indices) {
                    statusModelArrayList[i].selected = true
                    filesToDelete.add(statusModelArrayList[i])
                }
                binding.selectAll.isChecked = true
            } else {
                for (i in statusModelArrayList.indices) {
                    statusModelArrayList[i].selected = false
                }
                binding.actionLay.visibility = View.GONE
            }
            myAdapter?.notifyDataSetChanged()
        }

    }

    private fun startServices() {
        val context = requireContext().applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, Intent(context, DataService::class.java))        } else {
            context.startService(Intent(context, DataService::class.java))        }
    }

    private fun loadWhatsappStatusesAbove10(){
        DataServiceAboveTenVersion.whatsAppStatus.observe(viewLifecycleOwner, Observer {

            if (it.isEmpty()) {
                emptyLay.visibility = View.VISIBLE
            } else {
                emptyLay.visibility = View.GONE
            }

            Log.d("fileDoc", "CheckVersion10: " + it)

            statusModelArrayList.clear()
            statusModelArrayList.addAll(it)
            myAdapter = context?.let { it1 -> StatusAdapter(statusModelArrayList, it1) }
            binding.statusRecycler.adapter = myAdapter
            myAdapter?.setOnCheckChangeListener(this)
            swipeRefreshLayout!!.isRefreshing = false
        })
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private fun requestPermissionQ() {
        val sm = requireContext().getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val intent = sm.primaryStorageVolume.createOpenDocumentTreeIntent()
        val startDir = "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"
        var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
        var scheme = uri.toString()
        scheme = scheme.replace("/root/", "/document/")
        scheme += "%3A$startDir"
        uri = Uri.parse(scheme)
        Log.d("URI", uri.toString())
        intent.putExtra("android.provider.extra.INITIAL_URI", uri)
        intent.putExtra("android.provider.extra.SHOW_ADVANCED", true)
        intent.setFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        )
        activityResultLauncher.launch(intent)
    }

    private fun arePermissionDenied(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return requireContext().contentResolver.persistedUriPermissions.size <= 0
        }
        for (permissions in PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(requireContext(), permissions) != PackageManager.PERMISSION_GRANTED) {
                return true
            }
        }
        return false
    }

    private val REQUEST_PERMISSIONS = 1234
    private val PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }


    private var activityResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.data
            Log.d("HEY: ", data.toString())
            data?.let { uri ->
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                val fileDoc = DocumentFile.fromTreeUri(requireContext(), uri)!!
                Log.d("fileDoc", ": $uri")
                Log.d("fileDoc", "fileDocmmm: $fileDoc")
                Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(context, "Failed to obtain URI", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun init() {
        binding.statusRecycler.layoutManager = GridLayoutManager(context, 3)
        dataService = DataService()
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