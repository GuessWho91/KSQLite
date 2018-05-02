package com.guesswho91.ksqlite

import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

/**
 * Created by Leo on 30.04.2018.
 */
interface DBObject: BaseColumns {

    val _DATATABLE_NAME: String

    fun insertInDB(db: SQLiteOpenHelper){
        KDBRequests.insertInDB(db, this)
    }

    fun insertInDBWithCheck(db: SQLiteOpenHelper, params: Array<String>){
        KDBRequests.insertInDBWithCheck(db, this, params)
    }

}

/**
 * Database object with unique identifier
 */
interface UUIDDBObject : DBObject {
    val uuid: String
    val name: String
}