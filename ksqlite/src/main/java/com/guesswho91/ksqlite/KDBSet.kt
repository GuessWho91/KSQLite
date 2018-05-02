package com.guesswho91.ksqlite

/**
 * Database structure declaration
 * Created by Leo on 30.04.2018.
 */
abstract class KDBSet {

    /**
     * @return string to create table structure
     */
    private fun getCreateString(TableClass: Class<*>): String {

        val createString = StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(TableClass.simpleName)
                .append(" (_id INTEGER PRIMARY KEY AUTOINCREMENT")

        val columnSet = TableClass.declaredFields

        for (i in columnSet.indices) {

            val columnName = columnSet[i].name

            // _ID, _COUNT, _DATATABLE_NAME
            if (columnName[0] == '_' || columnName[0] == '$'
                    || columnName == "serialVersionUID" || columnName == "Companion")
                continue

            createString.append(", ").append(columnName)
            createString.append(getFieldType(columnName))
        }

        if (createString.contains("extid") && !createString.contains("extid_1c"))
            createString.append(", unique (extid)")
        if (createString.contains("uuid") && !createString.contains("doc_uuid"))
            createString.append(", unique (uuid)")

        createString.append(")")

        return createString.toString()

    }

    /**
     * @return string to create database structure
     */
    fun getDBCreateString(): Array<String?> {

        val tableSet = this::class.java.classes

        val dbCreateString = arrayOfNulls<String>(tableSet.size)

        for (i in tableSet.indices)
            dbCreateString[i] = getCreateString(tableSet[i])

        return dbCreateString

    }

    /**
     * @return the field string value type
     */
    abstract fun getFieldType(field: String): String

}