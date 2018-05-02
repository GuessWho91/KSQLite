# KSQLite

This is a kotlin library wich will make your work with SQLite database much more easier in more objective way.

## Getting Started

### Installing

### Using

1) Create a class where you will declare your database structure with `KDBSet` superclass.
There you need to create data classes with `DBObject` superclass and `@SVN` annotation similar to fields name.

<b>Note</b>: `@SVN` is using for constructor fields reflection so it is compulsory.

```
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
    
    override fun getFieldType(field: String): String { //method to set type of fields values
            return when (field){
                "uuid" -> " TEXT UNIQUE"
                "count" -> " INTEGER"
                "price" -> " NUMERIC"
                else -> " TEXT"
            }
        }

}
```
2) After database structure declared you need to make `SQLiteOpenHelper` singleton

```
class mDBHelper private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        val dbCreateString = mdbSet().getDBCreateString() //this method will create db structure 
based on previos class

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
```

3) Thats all! Now you can insert/update/get information from SQLite database

- To <b>insert</b> data you just need to create class instance and then call `insertInDB` method

```
mdbSet.client("uuid1", "Google inc").insertInDB(db)
mdbSet.client("uuid2", "Apple inc").insertInDB(db)
mdbSet.client_properties("uuid1", "some information").insertInDBWithCheck(db, arrayOf("client", "info"))
```

<b>Note</b>: You need to create `uuid` field in your table if you want to have uniqie fields. Data with the similar uuid will be updated. If your table doesn't have uuid, but you still need unique rows call `insertInDBWithCheck` where in second params you need to send array of field name which values association you want to be unique.

- To <b>get</b> data use methods of `KDBRequests` class

```
val db = mDBHelper.getInstance(this)

val clients = KDBRequests.getFromDBAll(db, mdbSet.client::class.java) // to get all rows from db table
print(clients) //Array ([0] client(uuid=uuid1, name=Google inc) [1] client(uuid=uuid2, name=Apple inc))

val google = KDBRequests.getFromDBByUuid(db, mdbSet.client::class.java, "uuid1")// to get row with unique uuid
print(google) //client(uuid=uuid1, name=Google inc)

val apple = KDBRequests.getFromDBByName(db, mdbSet.client::class.java, "Apple")// to get rows by name
print(apple) //Array ([0] client(uuid=uuid2, name=Apple inc))

val prop = KDBRequests.getFromDBByCustomParam(db, mdbSet.client_properties::class.java, "info", "some information") // to get row by custom field
print(prop) //Array ([0] client_properties(client=uuid1, info=some information))

```

## Authors

* **GuessWho91** - *Initial work* - [GuessWho91](https://github.com/GuessWho91)

Email for contact leo141291@mail.ru

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details