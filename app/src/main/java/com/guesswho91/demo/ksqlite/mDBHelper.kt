package com.guesswho91.demo.ksqlite
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Синглтон для работы с БД
 * Created by Leo on 05.07.17.
 */

class mDBHelper private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        val dbCreateString = mdbSet().getDBCreateString()

        dbCreateString.filter { it!!.isNotEmpty() }.forEach { db.execSQL(it) }

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {

        private var sInstance: mDBHelper? = null

        private const val DATABASE_NAME = "ksqlite.db" //here is you db name
        private const val DATABASE_VERSION = 1 //here is you db version

        @Synchronized fun getInstance(context: Context): mDBHelper { //singleton instance

            if (sInstance == null) {
                sInstance = mDBHelper(context)
            }

            return sInstance as mDBHelper

        }
    }

}