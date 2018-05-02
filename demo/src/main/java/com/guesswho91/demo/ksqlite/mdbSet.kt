package com.guesswho91.demo.ksqlite

import com.guesswho91.ksqlite.*

/**
 * Example of database structure declaration
 * Created by Leo on 30.04.2018.
 */
class mdbSet: KDBSet() {

    data class client (
            @SVN("uuid")    override val uuid: String,
            @SVN("name")    override val name: String): UUIDDBObject {

        override val _DATATABLE_NAME = "client"
    }

    data class client_properties(@SVN("client")    val client: String,
                                 @SVN("info")      val info: String) : DBObject {

        override val _DATATABLE_NAME: String = "client_properties"
    }

    override fun getFieldType(field: String): String {
        return when (field){
            "uuid" -> " TEXT UNIQUE"
            "count" -> " INTEGER"
            "price" -> " NUMERIC"
            else -> " TEXT"
        }
    }

}