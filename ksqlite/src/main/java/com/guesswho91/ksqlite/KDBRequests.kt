package com.guesswho91.ksqlite

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.ByteArrayOutputStream
import java.lang.reflect.Constructor
import java.util.*
import kotlin.collections.ArrayList

/**
 * Methods to working with database
 * Created by Leo on 03.07.17.
 */

class KDBRequests {

    companion object {

        /**
         * insert/update object in database table
         * @param db - database instance
         * @param element - object to insert/update
         * @param table - table name (getting from element by default)
         * @return number of insert row
         * {@link android.database.sqlite.SQLiteOpenHelper#getWritableDatabase#insert}
         */
        fun insertInDB (db: SQLiteOpenHelper, element: DBObject, table: String = element._DATATABLE_NAME): Long {

            val contVals = ContentValues()

            element.javaClass.declaredFields.forEach {
                if (it.name[0] == '_' || it.name[0] == '$'
                        || it.name == "serialVersionUID" || it.name == "Companion" )
                else
                    contVals.put(it.name, element.javaClass.getMethod("get" + it.name.capitalize()).invoke(element).toString())
            }

            try {

                if (contVals.containsKey("uuid") && contVals.get("uuid").toString().isNotEmpty())
                    db.writableDatabase.delete(table, "uuid = '${contVals.get("uuid")}'", null)

                return db.writableDatabase.insertOrThrow(table, null, contVals)

            } catch (e: Exception){

                if (contVals.containsKey("uuid") && contVals.get("uuid").toString().isNotEmpty())
                    return db.writableDatabase.update(table, contVals, "uuid = '${contVals.get("uuid")}'", null).toLong()

            }

            return 0
        }

        /**
         * Insert or update object in database by custom field
         * use this method instead {@link insertInDB} if there is no uuid field
         * and rows should be unique
         * @param checkFields - field names array
         */

        fun insertInDBWithCheck (db: SQLiteOpenHelper, element: DBObject,
                                 checkFields: Array<String>, table: String = element._DATATABLE_NAME): Long {

            val contVals = ContentValues()
            var whereClause = ""

            element.javaClass.declaredFields.forEach {
                if (it.name[0] == '_' || it.name[0] == '$'
                        || it.name == "serialVersionUID" || it.name == "Companion" ){

                } else {
                    val value = element.javaClass.getMethod("get" + it.name.capitalize()).invoke(element).toString()
                    contVals.put(it.name, value)

                    if (checkFields.contains(it.name))
                        whereClause += "${it.name} = '$value' AND "
                }

            }

            whereClause = if (whereClause.isNotEmpty()) whereClause.substring(0, whereClause.length - 4) else whereClause

            db.writableDatabase.delete(table, whereClause, null)
            return db.writableDatabase.insertOrThrow(table, null, contVals)

        }

        /**
         * Create instance of table element
         * @return Array of objects
         */
        private fun <T: DBObject> createInstance(cursor: Cursor, constructor: Constructor<T>): ArrayList<T> {
            val TList = ArrayList<T>()

            while (cursor.moveToNext()) {

                val constructorParameters = ArrayList<Any>()

                constructor.parameterAnnotations.forEachIndexed { index, annotation ->
                    if (annotation.isNotEmpty()) {

                        val e = (annotation[0] as SVN).expression

                        when (constructor.parameterTypes[index]){
                            Int::class.java -> constructorParameters.add(cursor.getInt(cursor.getColumnIndex(e)))
                            Double::class.java -> constructorParameters.add(cursor.getDouble(cursor.getColumnIndex(e)))
                            Float::class.java -> constructorParameters.add(cursor.getFloat(cursor.getColumnIndex(e)))
                            Bitmap::class.java -> {
                                val bb = cursor.getBlob(cursor.getColumnIndex(e))
                                constructorParameters.add(BitmapFactory.decodeByteArray(bb, 0, bb.size))
                            }
                            Int::class.java -> constructorParameters.add(cursor.getBlob(cursor.getColumnIndex(e)))
                            Boolean::class.java -> constructorParameters.add(cursor.getString(cursor.getColumnIndex(e)) == "true")
                            else -> constructorParameters.add(cursor.getString(cursor.getColumnIndex(e)))
                        }
                    }
                }
                try {
                    val element = constructor.newInstance(*constructorParameters.toArray()) as T
                    TList.add(element)
                } catch (e: Exception){
                    e.printStackTrace()
                }

            }

            return TList
        }

        /**
         * Getting from db by custom query
         * @param db - db instance
         * @param clazz - element class to get instance of
         * @param query - db query string
         * @return Array of class instance
         */
        fun <T: DBObject> getFromDB(db: SQLiteOpenHelper, clazz: Class<T>, query: String): ArrayList<T> {

            //check income params
//            val TableSet = mdbSet::class.java.classes
//            if (!TableSet.contains(clazz)) {
//                Log.e(this::class.java.simpleName, "Clazz must be declared in mdbSet!!")
//                return ArrayList()
//            }

            if (query.isEmpty()) {
                Log.e(this::class.java.simpleName, "query should't be empty")
                return ArrayList()
            }

            val cursor = db.readableDatabase.rawQuery(query, null)
            val constructor = clazz.constructors[0] as Constructor<T>
            return createInstance(cursor, constructor)
        }

        /**
         * Getting array of element by unique uuid
         * {@link #getFromDB}
         */
        fun <T: DBObject> getFromDBByUuid (db: SQLiteOpenHelper, clazz: Class<T>, uuid: String): T? {

            val query = "SELECT * FROM ${clazz.simpleName} WHERE uuid = '$uuid'"
            val TList = getFromDB(db, clazz, query)

            return if (TList.size > 0) TList[0]
            else null

        }

        /**
         * Getting array of element by only one custom field params
         * {@link #getFromDB}
         */
        fun <T: DBObject> getFromDBByCustomParam (db: SQLiteOpenHelper, clazz: Class<T>, param: String, value: String): ArrayList<T> {

            try {
                clazz.getDeclaredField(param)
            } catch (e: NoSuchFieldException){
                Log.e(clazz.simpleName, e.toString())
                return ArrayList()
            }

            val query = "SELECT * FROM ${clazz.simpleName} WHERE $param LIKE '$value'"
            return getFromDB(db, clazz, query)

        }

        /**
         * Getting array of element by custom fields params
         * {@link #getFromDB}
         */
        fun <T: DBObject> getFromDBByCustomParams (db: SQLiteOpenHelper, clazz: Class<T>,
                                                                             params: ArrayList<String>, values: ArrayList<String>): ArrayList<T> {
            if (params.size < 1 || params.size != values.size)
                return  ArrayList()

            var query_where = ""
            try {
                params.forEachIndexed { index, it->

                    clazz.getDeclaredField(it)
                    query_where+=" $it LIKE '${values[index]}'"

                    if (index < params.size - 1)
                        query_where += " AND "

                }
            } catch (e: NoSuchFieldException){
                Log.e(clazz.simpleName, e.toString())
                return ArrayList()
            }

            val query = "SELECT * FROM ${clazz.simpleName} WHERE $query_where"
            return getFromDB(db, clazz, query)
        }

        /**
         * Get count of element in db
         * {@link #getFromDB}
         */
        fun <T: DBObject> getCountInDBByCustomParams (db: SQLiteOpenHelper, clazz: Class<T>, params: HashMap<String, String>): Int {

            try {
                params.forEach {
                    entry -> clazz.getDeclaredField(entry.key)
                }
            } catch (e: NoSuchFieldException){
                Log.e(clazz.simpleName, e.toString())
                return 0
            }

            var query = "SELECT count(*) FROM ${clazz.simpleName} WHERE "
            params.forEach {
                entry ->
                query += "${entry.key} LIKE '${entry.value}' "
            }
            val cursor = db.readableDatabase.rawQuery(query, null)

            val count = if (cursor.moveToFirst()) cursor.getInt(0)
            else 0

            cursor.close()

            return count

        }

        /**
         * Get elements from db by name (search)
         * {@link #getFromDB}
         */
        fun <T: DBObject> getFromDBByName (db: SQLiteOpenHelper, clazz: Class<T>, search: String): ArrayList<T> {

            //checking fields
            val haveName = try {
                clazz.getDeclaredField("name")
                true
            } catch (e: NoSuchFieldException){
                false
            }

            val haveNamelow = try {
                clazz.getDeclaredField("namelow")
                true
            } catch (e: NoSuchFieldException){
                false
            }

            val haveNumber = try {
                clazz.getDeclaredField("number")
                true
            } catch (e: NoSuchFieldException){
                false
            }

            //creating query
            if (!haveName && !haveNamelow && !haveNumber)
                return ArrayList()

            var query = "SELECT * FROM ${clazz.simpleName} WHERE "
            if (haveNamelow)
                query += "namelow LIKE '%$search%' "
            else if (haveName)
                query += "name LIKE '%$search%' "

            if (haveNumber){
                if (haveName || haveNamelow)
                    query += "OR "
                query += "number LIKE '%$search%'"
            }

            try {
                clazz.getDeclaredField("isgroup")
                query += " ORDER BY isgroup DESC"
            } catch (e: NoSuchFieldException){
                try {
                    clazz.getDeclaredField("name")
                    query += " ORDER BY name DESC"
                } catch (e: NoSuchFieldException){ }
            }

            //searching...
            return getFromDB(db, clazz, query)

        }

        /**
         * Get all elements form table
         * {@link #getFromDB}
         */
        fun <T: DBObject> getFromDBAll (db: SQLiteOpenHelper, clazz: Class<T>): ArrayList<T> {

            val query = "SELECT * FROM ${clazz.simpleName} "
            return getFromDB(db, clazz, query)

        }

        /**
         * Getting byte array from bitmap to save it in db
         * {@link #getFromDB}
         */
        private fun getBitmapAsByteArray(bitmap: Bitmap): ByteArray {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream)
            return outputStream.toByteArray()
        }

    }

}