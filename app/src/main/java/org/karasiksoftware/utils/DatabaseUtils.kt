package org.karasiksoftware.utils

import org.karasiksoftware.dataclasses.database.DatabaseUserTokenData
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.karasiksoftware.dataclasses.database.DatabaseMeetingsData

class DatabaseUtils(
    private val context: Context
) {
    companion object {
        private const val USER_TABLE_NAME = "USER_TABLE"
        private const val MEETINGS_TABLE_NAME = "MEETINGS_TABLE"
        private const val TOKEN = "TOKEN"
        private const val MEETING_ID = "ID"
        private const val MEETINGS_SIZE = "MEETINGS_SIZE"
        private const val MEETINGS_INDEX = "MEETINGS_INDEX"
        private const val MEETINGS_NAMES = "MEETINGS_NAMES"
        private const val MEETINGS_STARTS = "MEETINGS_STARTS"
        private const val MEETINGS_ENDS = "MEETINGS_ENDS"
        private const val MEETINGS_AUDS = "MEETINGS_AUDS"
        const val DATABASE_NAME = "DATA"
    }
    private var database: SQLiteDatabase = this.context.openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null)

    private fun isTableExists(database: SQLiteDatabase, tableName: String): Boolean {
        val cursor: Cursor = database.rawQuery(
            "select DISTINCT tbl_name from sqlite_master where tbl_name = '$tableName'",
            null
        )

        if (cursor.count > 0) {
            cursor.close()
            return true
        }

        cursor.close()
        return false
    }

    @SuppressLint("Range")
    fun getUserToken(): DatabaseUserTokenData {
        if (!isTableExists(database, USER_TABLE_NAME)) {
            val values = ContentValues()
            values.put("ID", 1)
            values.put(TOKEN, "")

            database.execSQL(
                "CREATE TABLE "
                        + USER_TABLE_NAME
                        + " (ID INTEGER PRIMARY KEY, TOKEN TEXT);"
            )
            database.insert(
                USER_TABLE_NAME,
                null,
                values
            )
        }

        val cursor: Cursor = database.rawQuery("select * from $USER_TABLE_NAME",null)
        cursor.moveToFirst()

        val token = cursor.getString(cursor.getColumnIndex(TOKEN))
        cursor.close()

        return DatabaseUserTokenData(token)
    }

    fun setUserToken(tokenArg: String) {
        if (!isTableExists(database, USER_TABLE_NAME)) {
            val values = ContentValues()
            values.put("ID", 1)
            values.put(TOKEN, "")

            database.execSQL(
                "CREATE TABLE "
                        + USER_TABLE_NAME
                        + " (ID INTEGER PRIMARY KEY, TOKEN TEXT);"
            )
            database.insert(
                USER_TABLE_NAME,
                null,
                values
            )
        }

        val values = ContentValues()
        values.put(TOKEN, tokenArg)

        database.update(
            USER_TABLE_NAME,
            values,
            "ID = ?",
            arrayOf(1.toString())
        )
    }

    @SuppressLint("Range")
    fun getMeetingsTableData(): DatabaseMeetingsData {
        if (!isTableExists(database, MEETINGS_TABLE_NAME)) {
            val values = ContentValues()
            values.put(MEETING_ID, 1)
            values.put(MEETINGS_SIZE, 0)
            values.put(MEETINGS_INDEX, 0)
            values.put(MEETINGS_NAMES, "")
            values.put(MEETINGS_STARTS, "")
            values.put(MEETINGS_ENDS, "")
            values.put(MEETINGS_AUDS, "")

            database.execSQL(
                "CREATE TABLE "
                        + MEETINGS_TABLE_NAME
                        + " (ID INTEGER PRIMARY KEY,"
                        + " MEETINGS_SIZE INTEGER,"
                        + " MEETINGS_INDEX INTEGER,"
                        + " MEETINGS_NAMES TEXT,"
                        + " MEETINGS_STARTS TEXT,"
                        + " MEETINGS_ENDS TEXT,"
                        + " MEETINGS_AUDS TEXT);"
            )
            database.insert(
                MEETINGS_TABLE_NAME,
                null,
                values
            )
        }

        val cursor: Cursor = database.rawQuery("select * from $MEETINGS_TABLE_NAME",null)

        cursor.moveToFirst()

        val meetingsSize = cursor.getInt(cursor.getColumnIndex(MEETINGS_SIZE))
        val meetingsIndex = cursor.getInt(cursor.getColumnIndex(MEETINGS_INDEX))
        val meetingsNames =
            mutableListOf<String>(cursor.getString(cursor.getColumnIndex(MEETINGS_NAMES)))
        val meetingsStarts =
            mutableListOf<String>(cursor.getString(cursor.getColumnIndex(MEETINGS_STARTS)))
        val meetingsEnds =
            mutableListOf<String>(cursor.getString(cursor.getColumnIndex(MEETINGS_ENDS)))
        val meetingsAuds =
            mutableListOf<String>(cursor.getString(cursor.getColumnIndex(MEETINGS_AUDS)))

        for (i in 1 until meetingsSize) {
            cursor.move(1)
            meetingsNames.add(cursor.getString(cursor.getColumnIndex(MEETINGS_NAMES)))
            meetingsStarts.add(cursor.getString(cursor.getColumnIndex(MEETINGS_STARTS)))
            meetingsEnds.add(cursor.getString(cursor.getColumnIndex(MEETINGS_ENDS)))
            meetingsAuds.add(cursor.getString(cursor.getColumnIndex(MEETINGS_AUDS)))
        }

        cursor.close()

        return DatabaseMeetingsData(
            meetingsSize,
            meetingsIndex,
            meetingsNames,
            meetingsStarts,
            meetingsEnds,
            meetingsAuds
        )
    }

    fun setMeetingsTableData(meetings: DatabaseMeetingsData) {
        val meetingsIndex = meetings.meetingsIndex
        val meetingsSize = meetings.meetingsSize
        val meetingsNames = meetings.meetingsNames
        val meetingsStarts = meetings.meetingsStarts
        val meetingsEnds = meetings.meetingsEnds
        val meetingsAuds = meetings.meetingsAuds

        if (!isTableExists(database, MEETINGS_TABLE_NAME)) {
            val values = ContentValues()
            values.put(MEETING_ID, 1)
            values.put(MEETINGS_SIZE, 0)
            values.put(MEETINGS_INDEX, 0)
            values.put(MEETINGS_NAMES, "")
            values.put(MEETINGS_STARTS, "")
            values.put(MEETINGS_ENDS, "")
            values.put(MEETINGS_AUDS, "")

            database.execSQL(
                "CREATE TABLE "
                        + MEETINGS_TABLE_NAME
                        + " (ID INTEGER PRIMARY KEY,"
                        + " MEETINGS_SIZE INTEGER,"
                        + " MEETINGS_INDEX INTEGER,"
                        + " MEETINGS_NAMES TEXT,"
                        + " MEETINGS_STARTS TEXT,"
                        + " MEETINGS_ENDS TEXT,"
                        + " MEETINGS_AUDS TEXT);"
            )
            database.insert(
                MEETINGS_TABLE_NAME,
                null,
                values
            )
        }

        database.delete(MEETINGS_TABLE_NAME, null, null)

        for (i in 0 until meetingsSize) {
            val values = ContentValues()
            values.put(MEETING_ID, i+1)
            values.put(MEETINGS_SIZE, meetingsSize)
            values.put(MEETINGS_INDEX, meetingsIndex)
            values.put(MEETINGS_NAMES, meetingsNames[i])
            values.put(MEETINGS_STARTS, meetingsStarts[i])
            values.put(MEETINGS_ENDS, meetingsEnds[i])
            values.put(MEETINGS_AUDS, meetingsAuds[i])

            database.insert(
                MEETINGS_TABLE_NAME,
                null,
                values
            )
        }
    }
}