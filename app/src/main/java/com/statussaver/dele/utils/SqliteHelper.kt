package com.statussaver.dele.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.statussaver.dele.model.ContactModel
import com.statussaver.dele.services.NotificationService

class SqliteHelper(val context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION) {

    companion object{
        private const val DATABASE_NAME = "Dele"
        private const val DATABASE_VERSION = 1

        private const val TABLE_CONTACT = "contacts"
        private const val TABLE_CHAT = "chats"
        private const val TABLE_COUNT = "counts"

        private const val KEY_CONTACT_ID = "id"
        private const val KEY_CONTACT_TICKER = "contactTicker"
        private const val KEY_CONTACT_NAME = "contactName"
        private const val KEY_CONTACT_LOGO = "contactLogo"
        private const val KEY_CONTACT_TEXT = "contactText"
        private const val KEY_CONTACT_TIME = "contactTime"

        private const val KEY_CHAT_ID = "id"
        private const val KEY_CONTACT_CID = "cid"
        private const val KEY_CONTACT_CNAME = "cName"
        private const val KEY_CHAT_TEXT = "chatText"
        private const val KEY_CHAT_TIME = "chatTime"
        private const val KEY_CHAT_TYPE = "chatType"

        private const val KEY_COUNT_ID = "id"
        private const val KEY_CONTACT_CCID = "ccid"
        private const val KEY_CONTACT_CCNAME = "ccName"
        private const val KEY_COUNT_NO = "countNo"
    }

    val TAG = "SqliteHelper"

    val CREATE_CONTACT_TABLE = "CREATE TABLE " + TABLE_CONTACT +
            "(" +
            KEY_CONTACT_ID + " INTEGER PRIMARY KEY," +
            KEY_CONTACT_TICKER + " TEXT," +
            KEY_CONTACT_NAME + " TEXT UNIQUE," +
            KEY_CONTACT_LOGO + " TEXT," +
            KEY_CONTACT_TEXT + " TEXT," +
            KEY_CONTACT_TIME + " INTEGER" +
            ")"

    val CREATE_CHAT_TABLE = "CREATE TABLE " + TABLE_CHAT +
            "(" +
            KEY_CHAT_ID + " INTEGER PRIMARY KEY," +
            KEY_CONTACT_CID + " INTEGER," +
            KEY_CONTACT_CNAME + " TEXT," +
            KEY_CHAT_TEXT + " TEXT," +
            KEY_CHAT_TYPE + " TEXT," +
            KEY_CHAT_TIME + " INTEGER" +
            ")"

    val CREATE_COUNT_TABLE = "CREATE TABLE " + TABLE_COUNT +
            "(" +
            KEY_COUNT_ID + " INTEGER PRIMARY KEY," +
            KEY_CONTACT_CCID + " INTEGER," +
            KEY_CONTACT_CCNAME + " TEXT UNIQUE," +
            KEY_COUNT_NO + " INTEGER" +
            ")"



    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_CONTACT_TABLE)
        db?.execSQL(CREATE_CHAT_TABLE)
        db?.execSQL(CREATE_COUNT_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion != newVersion) {
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACT")
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_CHAT")
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_COUNT")
            onCreate(db)
        }
    }


    fun addContactID(contactModel: ContactModel): Long {

        //Log.d(TAG, "addContactID: "+new Gson().toJson(contactModel));
        val db = writableDatabase
        db.beginTransaction()
        var id: Long = 0
        try {
            val values = ContentValues()
            values.put(KEY_CONTACT_TICKER, contactModel.ticker)
            values.put(KEY_CONTACT_NAME, contactModel.name)
            values.put(KEY_CONTACT_LOGO, contactModel.logo)
            values.put(KEY_CONTACT_TEXT, contactModel.text)
            values.put(KEY_CONTACT_TIME, contactModel.time)
            val rows = db.update(
                TABLE_CONTACT,
                values,
                "$KEY_CONTACT_NAME= ?",
                arrayOf(contactModel.name)
            )
            if (rows == 1) {
                val usersSelectQuery = String.format(
                    "SELECT %s FROM %s WHERE %s = ?",
                    KEY_CONTACT_ID, TABLE_CONTACT, KEY_CONTACT_NAME
                )
                val cursor = db.rawQuery(
                    usersSelectQuery,
                    arrayOf(java.lang.String.valueOf(contactModel.name))
                )
                try {
                    if (cursor!!.moveToFirst()) {
                        id = cursor.getInt(0).toLong()
                        addChat(
                            contactModel.text,
                            id,
                            contactModel.time,
                            contactModel.name,
                            contactModel.type
                        )
                        db.setTransactionSuccessful()
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed) {
                        cursor.close()
                    }
                }
            } else {
                id = db.insertOrThrow(TABLE_CONTACT, null, values)
                addChat(
                    contactModel.text,
                    id,
                    contactModel.time,
                    contactModel.name,
                    contactModel.type
                )
                db.setTransactionSuccessful()
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error while trying to add contact to database" + e.message)
        } finally {
            db.endTransaction()
        }
        return id
    }

    fun addChat(contactModel: String?, cid: Long, time: Long, cText: String?, type: String?) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_CHAT_ID, time)
            values.put(KEY_CONTACT_CID, cid)
            values.put(KEY_CHAT_TEXT, contactModel)
            values.put(KEY_CONTACT_CNAME, cText)
            values.put(KEY_CHAT_TIME, time)
            values.put(KEY_CHAT_TYPE, type)
            db.insertOrThrow(TABLE_CHAT, null, values)
            if (type == "other")
                addCount(cid, cText)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.d(TAG, "Error while trying to add chat to database")
        } finally {
            db.endTransaction()
        }
    }

    fun addCount(cid: Long, name: String?) {
        name?.let {
            if (checkIfMyTitleExists(it)) {
                //int counts = getCountsData(name);
                val counts = getName(it)
                val countNo = counts + 1
                Log.d(TAG, "addCount: counts...$countNo")
                val db = writableDatabase
                db.beginTransaction()
                try {
                    val values = ContentValues()
                    values.put(KEY_CONTACT_CCID, cid)
                    values.put(KEY_CONTACT_CCNAME, it)
                    values.put(KEY_COUNT_NO, countNo)

                    db.update(TABLE_COUNT, values, "ccName = ?", arrayOf(it))
                    db.setTransactionSuccessful()
                } catch (e: Exception) {
                    Log.d(TAG, "Error while trying to addCount to database")
                } finally {
                    db.endTransaction()
                }
            }
            else {
                val db = writableDatabase
                db.beginTransaction()
                try {
                    val values = ContentValues()
                    values.put(KEY_CONTACT_CCID, cid)
                    values.put(KEY_COUNT_NO, 1)
                    values.put(KEY_CONTACT_CCNAME, it)
                    db.insert(TABLE_COUNT, null, values)
                    db.setTransactionSuccessful()
                } catch (e: Exception) {
                    Log.d(TAG, "Error while trying to add count to database")
                } finally {
                    db.endTransaction()
                }
            }
        }
    }

    @SuppressLint("Range")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getAllData(): List<ContactModel> {
        val arrayList: MutableList<ContactModel> = ArrayList<ContactModel>()
        val db = this.writableDatabase
        val cc = db.rawQuery("SELECT * FROM $TABLE_CONTACT", null)
        cc.moveToFirst()
        while (!cc.isAfterLast) {
            val bean = ContactModel()
            bean.ticker = cc.getString(cc.getColumnIndex(KEY_CONTACT_TICKER))
            bean.name = cc.getString(cc.getColumnIndex(KEY_CONTACT_NAME))
            bean.logo = cc.getString(cc.getColumnIndex(KEY_CONTACT_LOGO))
            bean.text = cc.getString(cc.getColumnIndex(KEY_CONTACT_TEXT))
            bean.time = cc.getLong(cc.getColumnIndex(KEY_CONTACT_TIME))
            bean.id = cc.getInt(cc.getColumnIndex(KEY_CONTACT_ID))
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                    for (model in NotificationService.notificationModels!!) {
                        if (model.name.equals(bean.name)) {
                            bean.icon = model.icon!!
                            break
                        }
                    }
                }
            } catch (e: Exception) {
            }
            arrayList.add(bean)
            Log.i("logo", cc.getString(cc.getColumnIndex(KEY_CONTACT_LOGO)))
            cc.moveToNext()
        }
        db.close()
        return arrayList
    }

    @SuppressLint("Range")
    fun getData(name: Int): MutableList<ContactModel> {
        val arrayList: MutableList<ContactModel> = ArrayList<ContactModel>()
        val db = this.writableDatabase
        val cc = db.rawQuery("SELECT * FROM $TABLE_CHAT WHERE $KEY_CONTACT_CID = $name", null)
        while (cc.moveToNext()) {
            val bean = ContactModel()
            bean.text = cc.getString(cc.getColumnIndex(KEY_CHAT_TEXT))
            bean.time = cc.getLong(cc.getColumnIndex(KEY_CHAT_TIME))
            bean.type = cc.getString(cc.getColumnIndex(KEY_CHAT_TYPE))
            arrayList.add(bean)
        }
        db.close()
        return arrayList
    }

    @SuppressLint("Range")
    fun getCountsData(id: String): Int {
        var countNo = 0
        val db = this.readableDatabase
        val cc = db.rawQuery(
            "SELECT * FROM $TABLE_COUNT WHERE $KEY_CONTACT_CCNAME = $id",
            null
        )
        cc.moveToFirst()
        while (!cc.isAfterLast) {
            countNo = cc.getInt(cc.getColumnIndex(KEY_COUNT_NO))
            cc.moveToNext()
        }
        db.close()
        return countNo
    }

    @SuppressLint("Range")
    fun getName(id: String): Int {
        var rv = 0
        val db = this.writableDatabase
        val whereclause = "ccName=?"
        val whereargs = arrayOf(id)
        val csr = db.query(TABLE_COUNT, null, whereclause, whereargs, null, null, null)
        if (csr.moveToFirst()) {
            rv = csr.getInt(csr.getColumnIndex(KEY_COUNT_NO))
        }
        return rv
    }

    fun checkIfMyTitleExists(title: String): Boolean {
        val db = this.writableDatabase
        val Query = "Select * from $TABLE_COUNT where $KEY_CONTACT_CCNAME = '$title'"
        val cursor = db.rawQuery(Query, null)
        if (cursor.count <= 0) {
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    fun resetCount(cid: Int, name: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_CONTACT_CCID, cid)
            values.put(KEY_COUNT_NO, 0)
            values.put(KEY_CONTACT_CCNAME, name)
            db.update(TABLE_COUNT, values, "$KEY_CONTACT_CCNAME= ?", arrayOf(name))
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.d(TAG, "Error while trying to reset to database")
        } finally {
            db.endTransaction()
        }
    }

    @SuppressLint("Range")
    fun getCountsDataByID(id: Int): Int {
        var countNo = 0
        val db = this.writableDatabase
        val cc = db.rawQuery(
            "SELECT * FROM $TABLE_COUNT WHERE $KEY_CONTACT_CCID = $id",
            null
        )
        cc.moveToFirst()
        while (!cc.isAfterLast) {
            countNo = cc.getInt(cc.getColumnIndex(KEY_COUNT_NO))
            cc.moveToNext()
        }
        db.close()
        return countNo
    }
}