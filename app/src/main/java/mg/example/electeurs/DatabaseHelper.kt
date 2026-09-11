package mg.example.electeurs

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "electeurs.db", null, 1) {

    companion object {
        private const val DATABASE_NAME = "electeurs.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_VOTERS = "voters"
    }

    override fun onCreate(db: SQLiteDatabase) {

        val createTable = """
            CREATE TABLE $TABLE_VOTERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                voter_id TEXT NOT NULL UNIQUE,
                name TEXT NOT NULL,
                birth_date TEXT,
                gender TEXT,
                region TEXT,
                district TEXT,
                fokontany TEXT
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_VOTERS")
        onCreate(db)
    }

    /**
     * Ajouter un nouvel électeur
     *
     * Retourne :
     * - l'ID créé si l'insertion réussit
     * - -1 si une erreur se produit
     */
    fun insertVoter(voter: Voter): Long {

        return try {

            val values = ContentValues().apply {

                put("voter_id", voter.voterId.trim())

                put("name", voter.name.trim())

                put(
                    "birth_date",
                    voter.birthDate.trim()
                )

                put(
                    "gender",
                    voter.gender.trim()
                )

                put(
                    "region",
                    voter.region.trim()
                )

                put(
                    "district",
                    voter.district.trim()
                )

                put(
                    "fokontany",
                    voter.fokontany.trim()
                )
            }

            writableDatabase.insertOrThrow(
                TABLE_VOTERS,
                null,
                values
            )

        } catch (e: Exception) {

            e.printStackTrace()

            -1L
        }
    }

    /**
     * Récupérer tous les électeurs
     */
    fun getAllVoters(): List<Voter> {

        val result = mutableListOf<Voter>()

        val cursor = readableDatabase.query(
            TABLE_VOTERS,
            null,
            null,
            null,
            null,
            null,
            "name ASC"
        )

        cursor.use {

            while (it.moveToNext()) {

                val voter = Voter(

                    id = it.getLong(
                        it.getColumnIndexOrThrow("id")
                    ),

                    voterId = it.getString(
                        it.getColumnIndexOrThrow("voter_id")
                    ) ?: "",

                    name = it.getString(
                        it.getColumnIndexOrThrow("name")
                    ) ?: "",

                    birthDate = it.getString(
                        it.getColumnIndexOrThrow("birth_date")
                    ) ?: "",

                    gender = it.getString(
                        it.getColumnIndexOrThrow("gender")
                    ) ?: "",

                    region = it.getString(
                        it.getColumnIndexOrThrow("region")
                    ) ?: "",

                    district = it.getString(
                        it.getColumnIndexOrThrow("district")
                    ) ?: "",

                    fokontany = it.getString(
                        it.getColumnIndexOrThrow("fokontany")
                    ) ?: ""
                )

                result.add(voter)
            }
        }

        return result
    }

    /**
     * Récupérer un électeur avec son ID SQLite
     */
    fun getVoter(id: Long): Voter? {

        val cursor = readableDatabase.query(

            TABLE_VOTERS,

            null,

            "id = ?",

            arrayOf(id.toString()),

            null,

            null,

            null
        )

        cursor.use {

            if (!it.moveToFirst()) {
                return null
            }

            return Voter(

                id = it.getLong(
                    it.getColumnIndexOrThrow("id")
                ),

                voterId = it.getString(
                    it.getColumnIndexOrThrow("voter_id")
                ) ?: "",

                name = it.getString(
                    it.getColumnIndexOrThrow("name")
                ) ?: "",

                birthDate = it.getString(
                    it.getColumnIndexOrThrow("birth_date")
                ) ?: "",

                gender = it.getString(
                    it.getColumnIndexOrThrow("gender")
                ) ?: "",

                region = it.getString(
                    it.getColumnIndexOrThrow("region")
                ) ?: "",

                district = it.getString(
                    it.getColumnIndexOrThrow("district")
                ) ?: "",

                fokontany = it.getString(
                    it.getColumnIndexOrThrow("fokontany")
                ) ?: ""
            )
        }
    }

    /**
     * Supprimer un électeur
     */
    fun deleteVoter(id: Long): Int {

        return try {

            writableDatabase.delete(
                TABLE_VOTERS,
                "id = ?",
                arrayOf(id.toString())
            )

        } catch (e: Exception) {

            e.printStackTrace()

            0
        }
    }

    /**
     * Rechercher un électeur
     */
    fun searchVoters(text: String): List<Voter> {

        val result = mutableListOf<Voter>()

        val search = "%${text.trim()}%"

        val cursor = readableDatabase.query(

            TABLE_VOTERS,

            null,

            """
                voter_id LIKE ?
                OR name LIKE ?
                OR region LIKE ?
                OR district LIKE ?
                OR fokontany LIKE ?
            """.trimIndent(),

            arrayOf(
                search,
                search,
                search,
                search,
                search
            ),

            null,

            null,

            "name ASC"
        )

        cursor.use {

            while (it.moveToNext()) {

                val voter = Voter(

                    id = it.getLong(
                        it.getColumnIndexOrThrow("id")
                    ),

                    voterId = it.getString(
                        it.getColumnIndexOrThrow("voter_id")
                    ) ?: "",

                    name = it.getString(
                        it.getColumnIndexOrThrow("name")
                    ) ?: "",

                    birthDate = it.getString(
                        it.getColumnIndexOrThrow("birth_date")
                    ) ?: "",

                    gender = it.getString(
                        it.getColumnIndexOrThrow("gender")
                    ) ?: "",

                    region = it.getString(
                        it.getColumnIndexOrThrow("region")
                    ) ?: "",

                    district = it.getString(
                        it.getColumnIndexOrThrow("district")
                    ) ?: "",

                    fokontany = it.getString(
                        it.getColumnIndexOrThrow("fokontany")
                    ) ?: ""
                )

                result.add(voter)
            }
        }

        return result
    }

    /**
     * Vérifie si un identifiant électeur existe déjà
     */
    fun voterIdExists(voterId: String): Boolean {

        val cursor = readableDatabase.query(

            TABLE_VOTERS,

            arrayOf("id"),

            "voter_id = ?",

            arrayOf(voterId.trim()),

            null,

            null,

            null,

            "1"
        )

        cursor.use {

            return it.moveToFirst()
        }
    }
}