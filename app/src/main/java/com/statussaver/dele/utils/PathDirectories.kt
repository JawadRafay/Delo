package com.statussaver.dele.utils

import android.os.Environment
import android.util.Log
import java.io.File

class PathDirectories {

    companion object{
        val MINI_KIND = 1
        val MICRO_KIND = 3
        val GRID_COUNT = 2
        var APP_DIR: String? = null
        private const val TAG = "PathDirectories"

        val STATUS_DIRECTORY = File(
            Environment.getExternalStorageDirectory().toString() +
                    File.separator + "WhatsApp/Media/.Statuses"
        )

        val STATUS_DIRECTORY_NEW = File(
            (Environment.getExternalStorageDirectory().toString() +
                    File.separator + "Android/media/com.whatsapp/WhatsApp/Media/.Statuses")
        )

        val VOICE_DIRECTORY = File(
            (Environment.getExternalStorageDirectory().toString() +
                    File.separator + "WhatsApp/Media/WhatsApp Voice Notes")
        )

        val VOICE_DIRECTORY_NEW = File(
            (Environment.getExternalStorageDirectory().toString() +
                    File.separator + "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes")
        )

        val AUDIO_DIRECTORY = File(
            (Environment.getExternalStorageDirectory().toString() +
                    File.separator + "WhatsApp/Media/WhatsApp Audio")
        )

        val AUDIO_DIRECTORY_NEW = File(
            (Environment.getExternalStorageDirectory().toString() +
                    File.separator + "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Audio")
        )

        val DOC_DIRECTORY = File(
            (Environment.getExternalStorageDirectory().toString() +
                    File.separator + "WhatsApp/Media/WhatsApp Documents")
        )

        val DOC_DIRECTORY_NEW = File(
            (Environment.getExternalStorageDirectory().toString() +
                    File.separator + "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents")
        )

        fun getWhatsappImages(): File {
            return if (File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "Android/media/com.whatsapp/WhatsApp" + File.separator + "Media" + File.separator + "WhatsApp Images"
                ).isDirectory
            ) {
                File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "Android/media/com.whatsapp/WhatsApp" + File.separator + "Media" + File.separator + "WhatsApp Images"
                )
            } else {
                File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "WhatsApp" + File.separator + "Media" + File.separator + "WhatsApp Images"
                )
            }
        }

        fun getWhatsappVideosFolder(): File {
            return if (File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "Android/media/com.whatsapp/WhatsApp" + File.separator + "Media" + File.separator + "WhatsApp Video"
                ).isDirectory
            ) {
                File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "Android/media/com.whatsapp/WhatsApp" + File.separator + "Media" + File.separator + "WhatsApp Video"
                )
            } else {
                File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "WhatsApp" + File.separator + "Media" + File.separator + "WhatsApp Video"
                )
            }
        }

        fun getWhatsappDocumentFolder(): File {
            return if (File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "Android/media/com.whatsapp/WhatsApp" + File.separator + "Media" + File.separator + "WhatsApp Documents"
                ).isDirectory
            ) {
                File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "Android/media/com.whatsapp/WhatsApp" + File.separator + "Media" + File.separator + "WhatsApp Documents"
                )
            } else {
                File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "WhatsApp" + File.separator + "Media" + File.separator + "WhatsApp Documents"
                )
            }
        }

        fun getWhatsappStatusFolder(): File {
            return if (File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "Android/media/com.whatsapp/WhatsApp" + File.separator + "Media" + File.separator + ".Statuses"
                ).isDirectory
            ) {
                File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "Android/media/com.whatsapp/WhatsApp" + File.separator + "Media" + File.separator + ".Statuses"
                )
            } else {
                File(
                    Environment.getExternalStorageDirectory() , File.separator + "WhatsApp/Media/.Statuses"
                )
            }
        }

        fun getDualWhatsappStatusFolder(): File {
            return if (File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "Android/media/com.whatsapp/DualApp/WhatsApp" + File.separator + "Media" + File.separator + ".Statuses"
                ).isDirectory
            ) {
                Log.d(TAG, "getDualWhatsappStatusFolder:if ")
                File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "Android/media/com.whatsapp/DualApp/WhatsApp" + File.separator + "Media" + File.separator + ".Statuses"
                )
            } else {
                Log.d(TAG, "getDualWhatsappStatusFolder:else ")
                File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "DualApp" + File.separator + "WhatsApp" + File.separator + "Media" + File.separator + ".Statuses"
                )
            }
        }

    }
}