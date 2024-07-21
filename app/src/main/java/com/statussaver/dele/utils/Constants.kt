package com.statussaver.dele.utils

import com.statussaver.dele.model.App

object Constants {
    const val PERMISSION_DIALOG_TITLE = "permission_dialog_title"
    const val PERMISSION_DIALOG_MSG = "permission_dialog_msg"
    const val PERMISSION_DIALOG_DENIED_TITLE = "permission_dialog_denied_title"
    const val PERMISSION_DIALOG_DENIED_MSG = "permission_dialog_denied_msg"
    const val PERMISSION_DIALOG_DENIED = "permission_dialog_denied"
    const val LOGS_DB_NAME = "logs_messages_db"
    const val NOTIFICATION_CHANNEL_ID = "dele"
    const val NOTIFICATION_CHANNEL_NAME = "delo_channel"

    enum class EnabledAppsDisplayType{
        VERTICAL,
        HORIZONTAL
    }

    // Beta feature flags
    const val BETA_FACEBOOK_SUPPORT_ENABLED = true

    /**
     * Set of apps this app can auto reply
     */
    @JvmField
    val SUPPORTED_APPS: Set<App> = setOf(
            App("WhatsApp", "com.whatsapp"),
            App("Facebook Messenger", "com.facebook.orca"),
            // App("Facebook Messenger Lite", "com.facebook.mlite"),
    )

    const val MIN_DAYS = 0
    const val MAX_DAYS = 7
    const val MIN_REPLIES_TO_ASK_APP_RATING = 5
    const val EMAIL_ADDRESS = "watomatic@deekshith.in"
    const val EMAIL_SUBJECT = "Watomatic-Feedback"
    const val TELEGRAM_URL = "tg://resolve?domain=watomatic"
}