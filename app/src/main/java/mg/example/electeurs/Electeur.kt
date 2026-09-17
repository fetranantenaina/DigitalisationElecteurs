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
// L'ENTITY — une table « electeurs », une ligne par électeur
// ---------------------------------------------------------------------------

@Entity(tableName = "electeurs")
data class Electeur(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val voterId: String,
    val cin: String,               // 12 chiffres (sans espaces)
    val name: String,
    val birthDate: String,
    val gender: String,
    val region: String,
    val district: String,
    val fokontany: String,
    val bureauVote: String,        // Bureau de vote
)

// ---------------------------------------------------------------------------
// Helpers : codes postaux par région + génération / formatage du CIN
// ---------------------------------------------------------------------------

fun codePostalDeRegion(region: String): String =
    REGIONS_MADAGASCAR
        .firstOrNull { it.nom.equals(region.trim(), ignoreCase = true) }
        ?.codePostal
        ?: "000"

fun formaterCin(cin: String): String {
    val brut = cin.filter { it.isDigit() }
    return brut.chunked(3).joinToString(" ")
}

fun genererCin(region: String, aleatoire: kotlin.random.Random = kotlin.random.Random.Default): String {
    val prefixe = codePostalDeRegion(region).padStart(3, '0')
    val suite = (1..9).joinToString("") { aleatoire.nextInt(0, 10).toString() }
    return prefixe + suite
}

// ---------------------------------------------------------------------------
// Codes de tri (utilisés dans le ORDER BY dynamique du DAO)
// ---------------------------------------------------------------------------

/** Définit les critères de tri disponibles pour la liste des électeurs. */
enum class TriElecteur(val code: Int, val libelle: String) {
    NOM_ASC(1, "Nom (A → Z)"),
    NOM_DESC(2, "Nom (Z → A)"),
    CIN_ASC(3, "CIN (croissant)"),
    CIN_DESC(4, "CIN (décroissant)"),
    ID_ASC(5, "ID (croissant)"),
    ID_DESC(6, "ID (décroissant)"),
}

// ---------------------------------------------------------------------------
// LE DAO — les requêtes réactives et de filtrage multi-critères
// ---------------------------------------------------------------------------

@Dao
interface ElecteurDao {

    /** Tous les électeurs triés par nom (Flow réactif). */
    @Query("SELECT * FROM electeurs ORDER BY name ASC")
    fun tousLesElecteurs(): Flow<List<Electeur>>

    /**
     * Recherche multi-critères + tri dynamique.
     *
     * Le paramètre [tri] est un code (voir [TriElecteur]) qui pilote la clause
     * ORDER BY via des CASE. On ne peut pas binder un nom de colonne en SQL,
     * donc on utilise cette technique : chaque CASE retourne une valeur
     * comparable, et on trie dessus.
     */
    @Query("""
        SELECT * FROM electeurs 
        WHERE (:text = '' OR voterId LIKE '%' || :text || '%' 
               OR cin LIKE '%' || :text || '%' 
               OR name LIKE '%' || :text || '%' 
               OR region LIKE '%' || :text || '%' 
               OR district LIKE '%' || :text || '%' 
               OR fokontany LIKE '%' || :text || '%'
               OR bureauVote LIKE '%' || :text || '%' )
          AND (:region IS NULL OR :region = '' OR region = :region)
          AND (:district IS NULL OR :district = '' OR district = :district)
          AND (:gender IS NULL OR :gender = '' OR gender = :gender)
        ORDER BY
          CASE WHEN :tri = 1 THEN name END ASC,
          CASE WHEN :tri = 2 THEN name END DESC,
          CASE WHEN :tri = 3 THEN cin END ASC,
          CASE WHEN :tri = 4 THEN cin END DESC,
          CASE WHEN :tri = 5 THEN id END ASC,
          CASE WHEN :tri = 6 THEN id END DESC,
          name ASC
    """)
    fun filtrerElecteurs(
        text: String,
        region: String?,
        district: String?,
        gender: String?,
        tri: Int
    ): Flow<List<Electeur>>

    /** Récupérer la liste distincte des régions pour alimenter les listes de sélection. */
    @Query("SELECT DISTINCT region FROM electeurs ORDER BY region ASC")
    suspend fun obtenirRegions(): List<String>

    /** Récupérer la liste distincte des districts (éventuellement filtrés par région). */
    @Query("SELECT DISTINCT district FROM electeurs WHERE (:region IS NULL OR :region = '' OR region = :region) ORDER BY district ASC")
    suspend fun obtenirDistricts(region: String?): List<String>

    /** Récupérer un électeur précis par son identifiant unique de base (pour l'écran détail). */
    @Query("SELECT * FROM electeurs WHERE id = :id")
    suspend fun parId(id: Long): Electeur?

    /** Vérifier si un numéro d'électeur (voterId) existe déjà. */
    @Query("SELECT EXISTS(SELECT 1 FROM electeurs WHERE voterId = :voterId)")
    suspend fun voterIdExiste(voterId: String): Boolean

    /** Vérifier si un CIN existe déjà. */
    @Query("SELECT EXISTS(SELECT 1 FROM electeurs WHERE cin = :cin)")
    suspend fun cinExiste(cin: String): Boolean

    /** Insérer un électeur unique. */
    @Insert
    suspend fun inserer(electeur: Electeur): Long

    /** Insérer une liste d'électeurs (jeu de données initial). */
    @Insert
    suspend fun insererTous(electeurs: List<Electeur>)

    /** Supprimer un électeur par son identifiant. */
    @Query("DELETE FROM electeurs WHERE id = :id")
    suspend fun supprimer(id: Long): Int
}

// ---------------------------------------------------------------------------
// LA DATABASE — le point d'assemblage Room
// ---------------------------------------------------------------------------

@Database(entities = [Electeur::class], version = 1, exportSchema = false)
abstract class ElecteurDatabase : RoomDatabase() {

    abstract fun electeurDao(): ElecteurDao

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
    Electeur(voterId = "RET001", cin = genererCin("Analamanga"), name = "Rakoto Jean", birthDate = "12/05/1990", gender = "Homme", region = "Analamanga", district = "Antananarivo Renivohitra", fokontany = "Isoraka", bureauVote = "EPP Isoraka"),
    Electeur(voterId = "RET002", cin = genererCin("Analamanga"), name = "Rasoa Marie", birthDate = "23/09/1985", gender = "Femme", region = "Analamanga", district = "Avaradrano", fokontany = "Sabotsy Namehana", bureauVote = "CEG Sabotsy Namehana"),
    Electeur(voterId = "RET003", cin = genererCin("Vakinankaratra"), name = "Andrianina Toky", birthDate = "04/11/1998", gender = "Homme", region = "Vakinankaratra", district = "Antsirabe I", fokontany = "Antsenakely", bureauVote = "Lycée Antsirabe"),
    Electeur(voterId = "RET004", cin = genererCin("Haute Matsiatra"), name = "Raharinirina Lalatiana", birthDate = "15/02/1972", gender = "Femme", region = "Haute Matsiatra", district = "Fianarantsoa I", fokontany = "Tanà Ambony", bureauVote = "EPP Tanà Ambony"),
    Electeur(voterId = "RET005", cin = genererCin("Atsinanana"), name = "Randria Patrick", birthDate = "30/07/1988", gender = "Homme", region = "Atsinanana", district = "Toamasina I", fokontany = "Tanambao V", bureauVote = "EPP Tanambao V"),
    Electeur(voterId = "RET006", cin = genererCin("Boeny"), name = "Soava Hanta", birthDate = "19/12/1995", gender = "Femme", region = "Boeny", district = "Mahajanga I", fokontany = "Mangarivotra", bureauVote = "CEG Mangarivotra"),
    Electeur(voterId = "RET007", cin = genererCin("Atsimo-Andrefana"), name = "Beandraibe William", birthDate = "08/06/1965", gender = "Homme", region = "Atsimo-Andrefana", district = "Toliara I", fokontany = "Besakoa", bureauVote = "EPP Besakoa"),
    Electeur(voterId = "RET008", cin = genererCin("Analamanga"), name = "Ravaka Faneva", birthDate = "27/03/2001", gender = "Femme", region = "Analamanga", district = "Atsimondrano", fokontany = "Ankadikely", bureauVote = "EPP Ankadikely"),
    Electeur(voterId = "RET009", cin = genererCin("SAVA"), name = "Zafinirina Marcel", birthDate = "11/10/1979", gender = "Homme", region = "SAVA", district = "Sambava", fokontany = "Anosibe", bureauVote = "EPP Anosibe"),
    Electeur(voterId = "RET010", cin = genererCin("Diana"), name = "Rasolofo Voahirana", birthDate = "05/01/1992", gender = "Femme", region = "Diana", district = "Antsiranana I", fokontany = "Mahajamba", bureauVote = "CEG Mahajamba"),
    Electeur(voterId = "RET011", cin = genererCin("Analamanga"), name = "Ravelojaona Eric", birthDate = "14/08/1978", gender = "Homme", region = "Analamanga", district = "Antananarivo Renivohitra", fokontany = "Analakely", bureauVote = "EPP Analakely"),
    Electeur(voterId = "RET012", cin = genererCin("Vakinankaratra"), name = "Bakoly Nadine", birthDate = "22/04/1983", gender = "Femme", region = "Vakinankaratra", district = "Antsirabe II", fokontany = "Betafo", bureauVote = "EPP Betafo"),
    Electeur(voterId = "RET013", cin = genererCin("Itasy"), name = "Ranaivoson Hery", birthDate = "10/12/1991", gender = "Homme", region = "Itasy", district = "Miarinarivo", fokontany = "Andolofotsy", bureauVote = "EPP Andolofotsy"),
    Electeur(voterId = "RET014", cin = genererCin("Bongolava"), name = "Sahondra Beatrice", birthDate = "03/06/1968", gender = "Femme", region = "Bongolava", district = "Tsiroanomandidy", fokontany = "Ampefy", bureauVote = "EPP Ampefy"),
    Electeur(voterId = "RET015", cin = genererCin("Atsinanana"), name = "Tsilavina Christian", birthDate = "18/09/2002", gender = "Homme", region = "Atsinanana", district = "Toamasina II", fokontany = "Mahavelona", bureauVote = "EPP Mahavelona"),
    Electeur(voterId = "RET016", cin = genererCin("Analanjirofo"), name = "Nomenjanahary Faratiana", birthDate = "25/01/1997", gender = "Femme", region = "Analanjirofo", district = "Fénérive Est", fokontany = "Tanambao", bureauVote = "EPP Tanambao"),
    Electeur(voterId = "RET017", cin = genererCin("Alaotra-Mangoro"), name = "Kely Paul", birthDate = "12/11/1959", gender = "Homme", region = "Alaotra-Mangoro", district = "Ambatondrazaka", fokontany = "Ambohijanahary", bureauVote = "EPP Ambohijanahary"),
    Electeur(voterId = "RET018", cin = genererCin("Amoron'i Mania"), name = "Zafimahazo Clémence", birthDate = "09/07/1984", gender = "Femme", region = "Amoron'i Mania", district = "Ambositra", fokontany = "Tandrokely", bureauVote = "EPP Tandrokely"),
    Electeur(voterId = "RET019", cin = genererCin("Haute Matsiatra"), name = "Rabetrano Tiana", birthDate = "16/03/1993", gender = "Homme", region = "Haute Matsiatra", district = "Ambalavao", fokontany = "Ambohimandroso", bureauVote = "EPP Ambohimandroso"),
    Electeur(voterId = "RET020", cin = genererCin("Vatovavy"), name = "Noromalala Vololonirina", birthDate = "30/10/1976", gender = "Femme", region = "Vatovavy", district = "Manakara", fokontany = "Tanambao Nord", bureauVote = "EPP Tanambao Nord"),
    Electeur(voterId = "RET021", cin = genererCin("Fitovinany"), name = "Fetra Mbolatiana", birthDate = "01/05/1999", gender = "Homme", region = "Fitovinany", district = "Vohipeno", fokontany = "Ankarongana", bureauVote = "EPP Ankarongana"),
    Electeur(voterId = "RET022", cin = genererCin("Atsimo-Atsinanana"), name = "Berthin Lova", birthDate = "14/02/1987", gender = "Femme", region = "Atsimo-Atsinanana", district = "Farafangana", fokontany = "Ampasimanjeha", bureauVote = "EPP Ampasimanjeha"),
    Electeur(voterId = "RET023", cin = genererCin("Ihorombe"), name = "Velomampionona Pascal", birthDate = "20/08/1970", gender = "Homme", region = "Ihorombe", district = "Ihosy", fokontany = "Mahatsinjo", bureauVote = "EPP Mahatsinjo"),
    Electeur(voterId = "RET024", cin = genererCin("Menabe"), name = "Anjarasoa Fenosoa", birthDate = "07/12/2000", gender = "Femme", region = "Menabe", district = "Morondava", fokontany = "Ankaboka", bureauVote = "EPP Ankaboka"),
    Electeur(voterId = "RET025", cin = genererCin("Atsimo-Andrefana"), name = "Mahafaly Solofo", birthDate = "19/06/1994", gender = "Homme", region = "Atsimo-Andrefana", district = "Betioky Sud", fokontany = "Behabatsy", bureauVote = "EPP Behabatsy"),
    Electeur(voterId = "RET026", cin = genererCin("Androy"), name = "Zazalahy Gilbert", birthDate = "05/10/1962", gender = "Homme", region = "Androy", district = "Ambovombe", fokontany = "Ampanihy", bureauVote = "EPP Ampanihy"),
    Electeur(voterId = "RET027", cin = genererCin("Anosy"), name = "Nambinintsoa Fara", birthDate = "11/04/1989", gender = "Femme", region = "Anosy", district = "Fort-Dauphin", fokontany = "Tanambao", bureauVote = "EPP Tanambao"),
    Electeur(voterId = "RET028", cin = genererCin("Melaky"), name = "Bemanana Jean Baptiste", birthDate = "28/11/1975", gender = "Homme", region = "Melaky", district = "Maintirano", fokontany = "Andovoka", bureauVote = "EPP Andovoka"),
    Electeur(voterId = "RET029", cin = genererCin("Sofia"), name = "Njarasoa Clarisse", birthDate = "13/03/1996", gender = "Femme", region = "Sofia", district = "Antsohihy", fokontany = "Ankerika", bureauVote = "EPP Ankerika"),
    Electeur(voterId = "RET030", cin = genererCin("Diana"), name = "Boto Ernest", birthDate = "22/07/1967", gender = "Homme", region = "Diana", district = "Nosy Be", fokontany = "Hell-Ville", bureauVote = "EPP Hell-Ville"),
    Electeur(voterId = "RET031", cin = genererCin("SAVA"), name = "Lalatiana Kanto", birthDate = "17/09/2003", gender = "Femme", region = "SAVA", district = "Vohemar", fokontany = "Antakotaka", bureauVote = "EPP Antakotaka"),
    Electeur(voterId = "RET032", cin = genererCin("Boeny"), name = "Tseheno Josoa", birthDate = "09/01/1991", gender = "Homme", region = "Boeny", district = "Mitsinjo", fokontany = "Katsepy", bureauVote = "EPP Katsepy"),
    Electeur(voterId = "RET033", cin = genererCin("Analamanga"), name = "Soamampionona Lantosoa", birthDate = "26/05/1982", gender = "Femme", region = "Analamanga", district = "Manjakandriana", fokontany = "Ambohibary", bureauVote = "EPP Ambohibary"),
    Electeur(voterId = "RET034", cin = genererCin("Analamanga"), name = "Heritiana Rija", birthDate = "03/10/1986", gender = "Homme", region = "Analamanga", district = "Ambohidratrimo", fokontany = "Ivato", bureauVote = "EPP Ivato"),
    Electeur(voterId = "RET035", cin = genererCin("Vakinankaratra"), name = "Razanamalala Berthine", birthDate = "15/12/1955", gender = "Femme", region = "Vakinankaratra", district = "Faratsiho", fokontany = "Ambohidava", bureauVote = "EPP Ambohidava"),
    Electeur(voterId = "RET036", cin = genererCin("Atsinanana"), name = "Mamy Nirina", birthDate = "21/02/1980", gender = "Homme", region = "Atsinanana", district = "Vatomandry", fokontany = "Tsarasoa", bureauVote = "EPP Tsarasoa"),
    Electeur(voterId = "RET037", cin = genererCin("Haute Matsiatra"), name = "Voahangy Malala", birthDate = "08/08/1992", gender = "Femme", region = "Haute Matsiatra", district = "Fianarantsoa II", fokontany = "Isandra", bureauVote = "EPP Isandra"),
    Electeur(voterId = "RET038", cin = genererCin("Itasy"), name = "Randrianarison Sedra", birthDate = "14/06/1995", gender = "Homme", region = "Itasy", district = "Arivonimamo", fokontany = "Ambaravarana", bureauVote = "EPP Ambaravarana"),
    Electeur(voterId = "RET039", cin = genererCin("Alaotra-Mangoro"), name = "Raharisoa Oliva", birthDate = "29/04/1973", gender = "Femme", region = "Alaotra-Mangoro", district = "Moramanga", fokontany = "Ambohibola", bureauVote = "EPP Ambohibola"),
    Electeur(voterId = "RET040", cin = genererCin("SAVA"), name = "Tovolahy Claude", birthDate = "11/11/1981", gender = "Homme", region = "SAVA", district = "Antalaha", fokontany = "Ampanefena", bureauVote = "EPP Ampanefena"),
    Electeur(voterId = "RET041", cin = genererCin("Diana"), name = "Fanantenana Zarasoa", birthDate = "06/07/1999", gender = "Femme", region = "Diana", district = "Ambanja", fokontany = "Ankatafa", bureauVote = "EPP Ankatafa"),
    Electeur(voterId = "RET042", cin = genererCin("Sofia"), name = "Gaston Jean Pierre", birthDate = "19/03/1964", gender = "Homme", region = "Sofia", district = "Befandriana Avaratra", fokontany = "Antetezambato", bureauVote = "EPP Antetezambato"),
    Electeur(voterId = "RET043", cin = genererCin("Analamanga"), name = "Ramanantsoa Harilala", birthDate = "25/10/1988", gender = "Femme", region = "Analamanga", district = "Antananarivo Renivohitra", fokontany = "Ankadifotsy", bureauVote = "EPP Ankadifotsy"),
    Electeur(voterId = "RET044", cin = genererCin("Menabe"), name = "Botoarimanana Haja", birthDate = "02/09/1977", gender = "Homme", region = "Menabe", district = "Miandrivazo", fokontany = "Ambakivao", bureauVote = "EPP Ambakivao"),
    Electeur(voterId = "RET045", cin = genererCin("Atsimo-Andrefana"), name = "Soline Zafinomenjanahary", birthDate = "18/12/1990", gender = "Femme", region = "Atsimo-Andrefana", district = "Morombe", fokontany = "Andranopasy", bureauVote = "EPP Andranopasy"),
    Electeur(voterId = "RET046", cin = genererCin("Amoron'i Mania"), name = "Randriamanantsoa Hery", birthDate = "31/01/1983", gender = "Homme", region = "Amoron'i Mania", district = "Fandriana", fokontany = "Sahambavy", bureauVote = "EPP Sahambavy"),
    Electeur(voterId = "RET047", cin = genererCin("Vatovavy"), name = "Zafindrasoa Honorine", birthDate = "12/08/1974", gender = "Femme", region = "Vatovavy", district = "Mananjary", fokontany = "Ambodinondry", bureauVote = "EPP Ambodinondry"),
    Electeur(voterId = "RET048", cin = genererCin("Analamanga"), name = "Njaka Manoa", birthDate = "04/05/2004", gender = "Homme", region = "Analamanga", district = "Antananarivo Renivohitra", fokontany = "Analamahitsy", bureauVote = "EPP Analamahitsy"),
    Electeur(voterId = "RET049", cin = genererCin("Bongolava"), name = "Ravaoarisoa Juliette", birthDate = "27/10/1961", gender = "Femme", region = "Bongolava", district = "Fenoarivobe", fokontany = "Ambohipandrano", bureauVote = "EPP Ambohipandrano"),
    Electeur(voterId = "RET050", cin = genererCin("Analanjirofo"), name = "Tsitohery Mbolatiana", birthDate = "16/02/1997", gender = "Homme", region = "Analanjirofo", district = "Sainte-Marie", fokontany = "Ambodifotatra", bureauVote = "EPP Ambodifotatra")
)