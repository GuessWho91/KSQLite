package com.guesswho91.demo.ksqlite

import android.app.Activity
import android.os.Bundle
import com.guesswho91.ksqlite.KDBRequests

/**
 * Working with KQSLITE example
 * Created by Leo on 30.04.2018.
 */
class MainActivity: Activity() {

    val db = mDBHelper.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initDemoDB()
        getFromDB()
    }

    /**
     * insert information in database
     * the logic is simple:
     * - create instance of class from mdbSet
     * - call insertInDB or insertInDBWithCheck
     */
    private fun initDemoDB() {

        mdbSet.client("uuid1", "Google inc").insertInDB(db)
        mdbSet.client("uuid2", "Apple inc").insertInDB(db)
        mdbSet.client_properties("uuid1", "some information").insertInDBWithCheck(db, arrayOf("client", "info"))

    }

    /**
     * Getting data from database
     */
    private fun getFromDB() {

        val db = mDBHelper.getInstance(this)

        val clients = KDBRequests.getFromDBAll(db, mdbSet.client::class.java) // to get all rows from db table
        print(clients) //Array ([0] client(uuid=uuid1, name=Google inc) [1] client(uuid=uuid2, name=Apple inc))
        val google = KDBRequests.getFromDBByUuid(db, mdbSet.client::class.java, "uuid1")// to get row with unique uuid
        print(google) //client(uuid=uuid1, name=Google inc)
        val apple = KDBRequests.getFromDBByName(db, mdbSet.client::class.java, "Apple")// to get rows by name
        print(apple) //Array ([0] client(uuid=uuid2, name=Apple inc))
        val prop = KDBRequests.getFromDBByCustomParam(db, mdbSet.client_properties::class.java, "info", "some information") // to get row by custom field
        print(prop) //Array ([0] client_properties(client=uuid1, info=some information))
    }
}