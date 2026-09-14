package mg.example.electeurs

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// ---------------------------------------------------------------------------
// L'ENTITY — une table « voters », une ligne par électeur
// ---------------------------------------------------------------------------

@Entity(tableName = "voters")
data class Voter(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val voterId: String,
    val name: String,
    val birthDate: String,
    val gender: String,
    val region: String,
    val district: String,
    val fokontany: String,
)

// ---------------------------------------------------------------------------
// LE DAO — les requêtes réactives et de filtrage multi-critères
// ---------------------------------------------------------------------------

@Dao
interface VoterDao {

    /** Tous les électeurs triés par nom (Flow réactif). */
    @Query("SELECT * FROM voters ORDER BY name ASC")
    fun tousLesElecteurs(): Flow<List<Voter>>

    /** Recherche multi-critères combinant texte libre, région, district et sexe. */
    @Query("""
        SELECT * FROM voters 
        WHERE (:text = '' OR voterId LIKE '%' || :text || '%' 
               OR name LIKE '%' || :text || '%' 
               OR region LIKE '%' || :text || '%' 
               OR district LIKE '%' || :text || '%' 
               OR fokontany LIKE '%' || :text || '%' )
          AND (:region IS NULL OR :region = '' OR region = :region)
          AND (:district IS NULL OR :district = '' OR district = :district)
          AND (:gender IS NULL OR :gender = '' OR gender = :gender)
        ORDER BY name ASC
    """)
    fun filtrerElecteurs(
        text: String,
        region: String?,
        district: String?,
        gender: String?
    ): Flow<List<Voter>>

    /** Récupérer la liste distincte des régions pour alimenter les listes de sélection. */
    @Query("SELECT DISTINCT region FROM voters ORDER BY region ASC")
    suspend fun obtenirRegions(): List<String>

    /** Récupérer la liste distincte des districts (éventuellement filtrés par région). */
    @Query("SELECT DISTINCT district FROM voters WHERE (:region IS NULL OR :region = '' OR region = :region) ORDER BY district ASC")
    suspend fun obtenirDistricts(region: String?): List<String>

    /** Récupérer un électeur précis par son identifiant unique de base (pour l'écran détail). */
    @Query("SELECT * FROM voters WHERE id = :id")
    suspend fun parId(id: Long): Voter?

    /** Vérifier si un numéro d'électeur (voterId) existe déjà. */
    @Query("SELECT EXISTS(SELECT 1 FROM voters WHERE voterId = :voterId)")
    suspend fun voterIdExiste(voterId: String): Boolean

    /** Insérer un électeur unique. */
    @Insert
    suspend fun inserer(voter: Voter): Long

    /** Insérer une liste d'électeurs (jeu de données initial). */
    @Insert
    suspend fun insererTous(voters: List<Voter>)

    /** Supprimer un électeur par son identifiant. */
    @Query("DELETE FROM voters WHERE id = :id")
    suspend fun supprimer(id: Long): Int
}

// ---------------------------------------------------------------------------
// LA DATABASE — le point d'assemblage Room
// ---------------------------------------------------------------------------

@Database(entities = [Voter::class], version = 1, exportSchema = false)
abstract class ElecteurDatabase : RoomDatabase() {

    abstract fun voterDao(): VoterDao

    companion object {
        @Volatile private var instance: ElecteurDatabase? = null

        fun obtenir(context: Context): ElecteurDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ElecteurDatabase::class.java,
                    "electeurs_room.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}

/** Jeu de données initial de 50 électeurs diversifiés. */
val electeursInitiaux = listOf(
    Voter(voterId = "RET001", name = "Rakoto Jean", birthDate = "12/05/1990", gender = "Homme", region = "Analamanga", district = "Antananarivo Renivohitra", fokontany = "Isoraka"),
    Voter(voterId = "RET002", name = "Rasoa Marie", birthDate = "23/09/1985", gender = "Femme", region = "Analamanga", district = "Avaradrano", fokontany = "Sabotsy Namehana"),
    Voter(voterId = "RET003", name = "Andrianina Toky", birthDate = "04/11/1998", gender = "Homme", region = "Vakinankaratra", district = "Antsirabe I", fokontany = "Antsenakely"),
    Voter(voterId = "RET004", name = "Raharinirina Lalatiana", birthDate = "15/02/1972", gender = "Femme", region = "Haute Matsiatra", district = "Fianarantsoa I", fokontany = "Tanà Ambony"),
    Voter(voterId = "RET005", name = "Randria Patrick", birthDate = "30/07/1988", gender = "Homme", region = "Atsinanana", district = "Toamasina I", fokontany = "Tanambao V"),
    Voter(voterId = "RET006", name = "Soava Hanta", birthDate = "19/12/1995", gender = "Femme", region = "Boeny", district = "Mahajanga I", fokontany = "Mangarivotra"),
    Voter(voterId = "RET007", name = "Beandraibe William", birthDate = "08/06/1965", gender = "Homme", region = "Atsimo-Andrefana", district = "Toliara I", fokontany = "Besakoa"),
    Voter(voterId = "RET008", name = "Ravaka Faneva", birthDate = "27/03/2001", gender = "Femme", region = "Analamanga", district = "Atsimondrano", fokontany = "Ankadikely"),
    Voter(voterId = "RET009", name = "Zafinirina Marcel", birthDate = "11/10/1979", gender = "Homme", region = "SAVA", district = "Sambava", fokontany = "Anosibe"),
    Voter(voterId = "RET010", name = "Rasolofo Voahirana", birthDate = "05/01/1992", gender = "Femme", region = "Diana", district = "Antsiranana I", fokontany = "Mahajamba"),
    Voter(voterId = "RET011", name = "Ravelojaona Eric", birthDate = "14/08/1978", gender = "Homme", region = "Analamanga", district = "Antananarivo Renivohitra", fokontany = "Analakely"),
    Voter(voterId = "RET012", name = "Bakoly Nadine", birthDate = "22/04/1983", gender = "Femme", region = "Vakinankaratra", district = "Antsirabe II", fokontany = "Betafo"),
    Voter(voterId = "RET013", name = "Ranaivoson Hery", birthDate = "10/12/1991", gender = "Homme", region = "Itasy", district = "Miarinarivo", fokontany = "Andolofotsy"),
    Voter(voterId = "RET014", name = "Sahondra Beatrice", birthDate = "03/06/1968", gender = "Femme", region = "Bongolava", district = "Tsiroanomandidy", fokontany = "Ampefy"),
    Voter(voterId = "RET015", name = "Tsilavina Christian", birthDate = "18/09/2002", gender = "Homme", region = "Atsinanana", district = "Toamasina II", fokontany = "Mahavelona"),
    Voter(voterId = "RET016", name = "Nomenjanahary Faratiana", birthDate = "25/01/1997", gender = "Femme", region = "Analanjirofo", district = "Fénérive Est", fokontany = "Tanambao"),
    Voter(voterId = "RET017", name = "Kely Paul", birthDate = "12/11/1959", gender = "Homme", region = "Alaotra-Mangoro", district = "Ambatondrazaka", fokontany = "Ambohijanahary"),
    Voter(voterId = "RET018", name = "Zafimahazo Clémence", birthDate = "09/07/1984", gender = "Femme", region = "Amoron'i Mania", district = "Ambositra", fokontany = "Tandrokely"),
    Voter(voterId = "RET019", name = "Rabetrano Tiana", birthDate = "16/03/1993", gender = "Homme", region = "Haute Matsiatra", district = "Ambalavao", fokontany = "Ambohimandroso"),
    Voter(voterId = "RET020", name = "Noromalala Vololonirina", birthDate = "30/10/1976", gender = "Femme", region = "Vatovavy", district = "Manakara", fokontany = "Tanambao Nord"),
    Voter(voterId = "RET021", name = "Fetra Mbolatiana", birthDate = "01/05/1999", gender = "Homme", region = "Fitovinany", district = "Vohipeno", fokontany = "Ankarongana"),
    Voter(voterId = "RET022", name = "Berthin Lova", birthDate = "14/02/1987", gender = "Femme", region = "Atsimo-Atsinanana", district = "Farafangana", fokontany = "Ampasimanjeha"),
    Voter(voterId = "RET023", name = "Velomampionona Pascal", birthDate = "20/08/1970", gender = "Homme", region = "Ihorombe", district = "Ihosy", fokontany = "Mahatsinjo"),
    Voter(voterId = "RET024", name = "Anjarasoa Fenosoa", birthDate = "07/12/2000", gender = "Femme", region = "Menabe", district = "Morondava", fokontany = "Ankaboka"),
    Voter(voterId = "RET025", name = "Mahafaly Solofo", birthDate = "19/06/1994", gender = "Homme", region = "Atsimo-Andrefana", district = "Betioky Sud", fokontany = "Behabatsy"),
    Voter(voterId = "RET026", name = "Zazalahy Gilbert", birthDate = "05/10/1962", gender = "Homme", region = "Androy", district = "Ambovombe", fokontany = "Ampanihy"),
    Voter(voterId = "RET027", name = "Nambinintsoa Fara", birthDate = "11/04/1989", gender = "Femme", region = "Anosy", district = "Fort-Dauphin", fokontany = "Tanambao"),
    Voter(voterId = "RET028", name = "Bemanana Jean Baptiste", birthDate = "28/11/1975", gender = "Homme", region = "Melaky", district = "Maintirano", fokontany = "Andovoka"),
    Voter(voterId = "RET029", name = "Njarasoa Clarisse", birthDate = "13/03/1996", gender = "Femme", region = "Sofia", district = "Antsohihy", fokontany = "Ankerika"),
    Voter(voterId = "RET030", name = "Boto Ernest", birthDate = "22/07/1967", gender = "Homme", region = "Diana", district = "Nosy Be", fokontany = "Hell-Ville"),
    Voter(voterId = "RET031", name = "Lalatiana Kanto", birthDate = "17/09/2003", gender = "Femme", region = "SAVA", district = "Vohemar", fokontany = "Antakotaka"),
    Voter(voterId = "RET032", name = "Tseheno Josoa", birthDate = "09/01/1991", gender = "Homme", region = "Boeny", district = "Mitsinjo", fokontany = "Katsepy"),
    Voter(voterId = "RET033", name = "Soamampionona Lantosoa", birthDate = "26/05/1982", gender = "Femme", region = "Analamanga", district = "Manjakandriana", fokontany = "Ambohibary"),
    Voter(voterId = "RET034", name = "Heritiana Rija", birthDate = "03/10/1986", gender = "Homme", region = "Analamanga", district = "Ambohidratrimo", fokontany = "Ivato"),
    Voter(voterId = "RET035", name = "Razanamalala Berthine", birthDate = "15/12/1955", gender = "Femme", region = "Vakinankaratra", district = "Faratsiho", fokontany = "Ambohidava"),
    Voter(voterId = "RET036", name = "Mamy Nirina", birthDate = "21/02/1980", gender = "Homme", region = "Atsinanana", district = "Vatomandry", fokontany = "Tsarasoa"),
    Voter(voterId = "RET037", name = "Voahangy Malala", birthDate = "08/08/1992", gender = "Femme", region = "Haute Matsiatra", district = "Fianarantsoa II", fokontany = "Isandra"),
    Voter(voterId = "RET038", name = "Randrianarison Sedra", birthDate = "14/06/1995", gender = "Homme", region = "Itasy", district = "Arivonimamo", fokontany = "Ambaravarana"),
    Voter(voterId = "RET039", name = "Raharisoa Oliva", birthDate = "29/04/1973", gender = "Femme", region = "Alaotra-Mangoro", district = "Moramanga", fokontany = "Ambohibola"),
    Voter(voterId = "RET040", name = "Tovolahy Claude", birthDate = "11/11/1981", gender = "Homme", region = "SAVA", district = "Antalaha", fokontany = "Ampanefena"),
    Voter(voterId = "RET041", name = "Fanantenana Zarasoa", birthDate = "06/07/1999", gender = "Femme", region = "Diana", district = "Ambanja", fokontany = "Ankatafa"),
    Voter(voterId = "RET042", name = "Gaston Jean Pierre", birthDate = "19/03/1964", gender = "Homme", region = "Sofia", district = "Befandriana Avaratra", fokontany = "Antetezambato"),
    Voter(voterId = "RET043", name = "Ramanantsoa Harilala", birthDate = "25/10/1988", gender = "Femme", region = "Analamanga", district = "Antananarivo Renivohitra", fokontany = "Ankadifotsy"),
    Voter(voterId = "RET044", name = "Botoarimanana Haja", birthDate = "02/09/1977", gender = "Homme", region = "Menabe", district = "Miandrivazo", fokontany = "Ambakivao"),
    Voter(voterId = "RET045", name = "Soline Zafinomenjanahary", birthDate = "18/12/1990", gender = "Femme", region = "Atsimo-Andrefana", district = "Morombe", fokontany = "Andranopasy"),
    Voter(voterId = "RET046", name = "Randriamanantsoa Hery", birthDate = "31/01/1983", gender = "Homme", region = "Amoron'i Mania", district = "Fandriana", fokontany = "Sahambavy"),
    Voter(voterId = "RET047", name = "Zafindrasoa Honorine", birthDate = "12/08/1974", gender = "Femme", region = "Vatovavy", district = "Mananjary", fokontany = "Ambodinondry"),
    Voter(voterId = "RET048", name = "Njaka Manoa", birthDate = "04/05/2004", gender = "Homme", region = "Analamanga", district = "Antananarivo Renivohitra", fokontany = "Analamahitsy"),
    Voter(voterId = "RET049", name = "Ravaoarisoa Juliette", birthDate = "27/10/1961", gender = "Femme", region = "Bongolava", district = "Fenoarivobe", fokontany = "Ambohipandrano"),
    Voter(voterId = "RET050", name = "Tsitohery Mbolatiana", birthDate = "16/02/1997", gender = "Homme", region = "Analanjirofo", district = "Sainte-Marie", fokontany = "Ambodifotatra")
)