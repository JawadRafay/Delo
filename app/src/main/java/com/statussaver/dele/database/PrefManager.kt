package com.statussaver.dele.database

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.statussaver.dele.R
import com.statussaver.dele.model.App
import com.statussaver.dele.utils.AppUtils
import com.statussaver.dele.utils.Constants
import java.util.*

class PrefManager(val context: Context) {

    private val PREF_NAME : String = "Dele"
    private var sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()

    //Data fields
    private val KEY_SERVICE_ENABLED = "pref_service_enabled"
    private val KEY_GROUP_REPLY_ENABLED = "pref_group_reply_enabled"
    private val KEY_AUTO_REPLY_THROTTLE_TIME_MS = "pref_auto_reply_throttle_time_ms"
    private val KEY_SELECTED_APPS_ARR = "pref_selected_apps_arr"
    private val KEY_IS_APPEND_WATOMATIC_ATTRIBUTION = "pref_is_append_watomatic_attribution"
    private val KEY_GITHUB_RELEASE_NOTES_ID = "pref_github_release_notes_id"
    private val KEY_PURGE_MESSAGE_LOGS_LAST_TIME = "pref_purge_message_logs_last_time"
    private val KEY_PLAY_STORE_RATING_STATUS = "pref_play_store_rating_status"
    private val KEY_PLAY_STORE_RATING_LAST_TIME = "pref_play_store_rating_last_time"
    private val KEY_SHOW_FOREGROUND_SERVICE_NOTIFICATION = "pref_show_foreground_service_notification"
    private val KEY_REPLY_CONTACTS = "pref_reply_contacts"
    private val KEY_REPLY_CONTACTS_TYPE = "pref_reply_contacts_type"
    private val KEY_REPLY_CUSTOM_NAMES = "pref_reply_custom_names"
    private val KEY_SELECTED_CONTACT_NAMES = "pref_selected_contacts_names"
    private var KEY_IS_SHOW_NOTIFICATIONS_ENABLED: String? = null
    private var KEY_SELECTED_APP_LANGUAGE: String? = null
    private val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
    private val MODE = "mode"
    private val DATE = "date"
    private val IS_PERMISSION_SET = "ispermission";


    init {
        // Use key from string resource

        // Use key from string resource
        KEY_SELECTED_APP_LANGUAGE = context.getString(R.string.key_pref_app_language)
        KEY_IS_SHOW_NOTIFICATIONS_ENABLED =
            context.getString(R.string.pref_show_notification_replied_msg)

        // For new installs, enable all the supported apps

        // For new installs, enable all the supported apps
        val newInstall = (!sharedPreferences.contains(KEY_SERVICE_ENABLED)
                && !sharedPreferences.contains(KEY_SELECTED_APPS_ARR))
        if (newInstall) {
            // Enable all supported apps for new install
            setAppsAsEnabled(Constants.SUPPORTED_APPS)

            // Set notifications ON for new installs
            setShowNotificationPref(true)
        }

        if (isFirstInstall(context)) {
            // Set Append Watomatic attribution checked for new installs
            if (!sharedPreferences.contains(KEY_IS_APPEND_WATOMATIC_ATTRIBUTION)) {
                setAppendWatomaticAttribution(true)
            }
        } else {
            //If it's first install, language preference is not set, so we don't have to worry
            //Otherwise, check if language settings contains r, migrate to new language settings key
            updateLegacyLanguageKey()
        }
    }

    fun isServiceEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_SERVICE_ENABLED, false)
    }

    fun setServicePref(enabled: Boolean) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(KEY_SERVICE_ENABLED, enabled)
        editor.apply()
    }

    fun isGroupReplyEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_GROUP_REPLY_ENABLED, false)
    }

    fun setGroupReplyPref(enabled: Boolean) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(KEY_GROUP_REPLY_ENABLED, enabled)
        editor.apply()
    }

    fun getAutoReplyDelay(): Long {
        return sharedPreferences.getLong(KEY_AUTO_REPLY_THROTTLE_TIME_MS, 0)
    }

    fun setAutoReplyDelay(delay: Long) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putLong(KEY_AUTO_REPLY_THROTTLE_TIME_MS, delay)
        editor.apply()
    }

    fun getEnabledApps(): MutableSet<String?> {
        var enabledAppsJsonStr: String? = sharedPreferences.getString(KEY_SELECTED_APPS_ARR, null)

        // Users upgrading from v1.7 and before
        // For upgrading users, preserve functionality by enabling only WhatsApp
        //   (remove this when time most users would have updated. May be in 3 weeks after deploying this?)
        if (enabledAppsJsonStr == null || enabledAppsJsonStr == "[]") {
            enabledAppsJsonStr = setAppsAsEnabled(setOf<App>(App("WhatsApp", "com.whatsapp")))
        }
        val type = object : TypeToken<Set<String?>?>() {}.type
        return Gson().fromJson(enabledAppsJsonStr, type)
    }

    fun isAppEnabled(thisApp: App): Boolean {
        return getEnabledApps().contains(thisApp.packageName)
    }

    private fun serializeAndSetEnabledPackageList(packageList: Collection<String?>): String? {
        val jsonStr = Gson().toJson(packageList)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(KEY_SELECTED_APPS_ARR, jsonStr)
        editor.apply()
        return jsonStr
    }

    private fun setAppsAsEnabled(apps: Collection<App?>): String? {
        val appUtils = AppUtils(context)
        val packageNames: MutableSet<String?> = HashSet()
        for (app in apps) {
            //check if the app is installed only then add it to enabled list
            if (appUtils.isPackageInstalled(app?.packageName)) {
                packageNames.add(app?.packageName)
            }
        }
        return serializeAndSetEnabledPackageList(packageNames)
    }

    fun saveEnabledApps(app: App, isSelected: Boolean): String? {
        val enabledPackages = getEnabledApps()
        if (!isSelected) {
            //remove the given platform
            enabledPackages.remove(app.packageName)
        } else {
            //add the given platform
            enabledPackages.add(app.packageName)
        }
        return serializeAndSetEnabledPackageList(enabledPackages)
    }

    private fun setAppendWatomaticAttribution(enabled: Boolean) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_APPEND_WATOMATIC_ATTRIBUTION, enabled)
        editor.apply()
    }

    fun isAppendWatomaticAttributionEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_APPEND_WATOMATIC_ATTRIBUTION, false)
    }

    /**
     * Check if it is first install on this device.
     * ref: https://stackoverflow.com/a/34194960
     * @param context
     * @return true if first install or else false if it is installed from an update
     */
    private fun isFirstInstall(context: Context): Boolean {
        return try {
            val firstInstallTime =
                context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
            val lastUpdateTime =
                context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
            firstInstallTime == lastUpdateTime
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            true
        }
    }

    fun getSelectedLanguageStr(defaultLangStr: String?): String? {
        return sharedPreferences.getString(KEY_SELECTED_APP_LANGUAGE, defaultLangStr)
    }

    fun setLanguageStr(languageStr: String?) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(KEY_SELECTED_APP_LANGUAGE, languageStr)
        editor.apply()
    }

    fun getSelectedLocale(): Locale? {
        val thisLangStr = getSelectedLanguageStr(null)
        if (thisLangStr == null || thisLangStr.isEmpty()) {
            return Locale.getDefault()
        }
        val languageSplit = thisLangStr.split("-").toTypedArray()
        return if (languageSplit.size == 2) Locale(languageSplit[0], languageSplit[1]) else Locale(
            languageSplit[0]
        )
    }

    private fun updateLegacyLanguageKey() {
        val thisLangStr = getSelectedLanguageStr(null)
        if (thisLangStr == null || thisLangStr.isEmpty()) {
            return
        }
        val languageSplit = thisLangStr.split("-").toTypedArray()
        if (languageSplit.size == 2) {
            if (languageSplit[1].length == 3) {
                val newLangStr = thisLangStr.replace("-r", "-")
                setLanguageStr(newLangStr)
            }
        }
    }

    fun isShowNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_SHOW_NOTIFICATIONS_ENABLED, false)
    }

    private fun setShowNotificationPref(enabled: Boolean) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_SHOW_NOTIFICATIONS_ENABLED, enabled)
        editor.apply()
    }

    fun getGithubReleaseNotesId(): Int {
        return sharedPreferences.getInt(KEY_GITHUB_RELEASE_NOTES_ID, 0)
    }

    fun setGithubReleaseNotesId(id: Int) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt(KEY_GITHUB_RELEASE_NOTES_ID, id)
        editor.apply()
    }

    fun getLastPurgedTime(): Long {
        return sharedPreferences.getLong(KEY_PURGE_MESSAGE_LOGS_LAST_TIME, 0)
    }

    fun setPurgeMessageTime(purgeMessageTime: Long) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putLong(KEY_PURGE_MESSAGE_LOGS_LAST_TIME, purgeMessageTime)
        editor.apply()
    }

    fun getPlayStoreRatingStatus(): String? {
        return sharedPreferences.getString(KEY_PLAY_STORE_RATING_STATUS, "")
    }

    fun setPlayStoreRatingStatus(status: String?) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(KEY_PLAY_STORE_RATING_STATUS, status)
        editor.apply()
    }

    fun getPlayStoreRatingLastTime(): Long {
        return sharedPreferences.getLong(KEY_PLAY_STORE_RATING_LAST_TIME, 0)
    }

    fun setPlayStoreRatingLastTime(purgeMessageTime: Long) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putLong(KEY_PLAY_STORE_RATING_LAST_TIME, purgeMessageTime)
        editor.apply()
    }

    fun setShowForegroundServiceNotification(enabled: Boolean) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(KEY_SHOW_FOREGROUND_SERVICE_NOTIFICATION, enabled)
        editor.apply()
    }

    fun isForegroundServiceNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_SHOW_FOREGROUND_SERVICE_NOTIFICATION, false)
    }

    fun setReplyToNames(names: Set<String?>?) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putStringSet(KEY_SELECTED_CONTACT_NAMES, names)
        editor.apply()
    }

    fun getReplyToNames(): Set<String?>? {
        return sharedPreferences.getStringSet(KEY_SELECTED_CONTACT_NAMES, HashSet<String>())
    }

    fun getCustomReplyNames(): Set<String?>? {
        return sharedPreferences.getStringSet(KEY_REPLY_CUSTOM_NAMES, HashSet<String>())
    }

    fun setCustomReplyNames(names: Set<String?>?) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putStringSet(KEY_REPLY_CUSTOM_NAMES, names)
        editor.apply()
    }

    fun isContactReplyEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_REPLY_CONTACTS, false)
    }

    fun isContactReplyBlacklistMode(): Boolean? {
        return sharedPreferences.getString(KEY_REPLY_CONTACTS_TYPE, "pref_blacklist") == "pref_blacklist"
    }

    fun setFirstTimeLaunch(isFirstTime: Boolean) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
        editor.commit()
    }

    fun isFirstTimeLaunch(): Boolean {
        return sharedPreferences.getBoolean(IS_FIRST_TIME_LAUNCH, true)
    }

    fun setDarkMode(isFirstTime: Boolean) {
        editor.putBoolean(MODE, isFirstTime)
        editor.commit()
    }

    fun getDarkMode(): Boolean {
        return sharedPreferences.getBoolean(MODE, false)
    }

    fun setDate(key: Long) {
        editor.putLong(DATE, key)
        editor.commit()
    }

    fun getDate(): Long {
        return sharedPreferences.getLong(DATE, 0)
    }

    fun setIsPermissionSet(isPermissionSet: Boolean?) {
        editor.putBoolean(IS_PERMISSION_SET, isPermissionSet!!)
        editor.commit()
    }

    fun getIsPermissionSet(): Boolean? {
        return sharedPreferences.getBoolean(IS_PERMISSION_SET, false)
    }
}