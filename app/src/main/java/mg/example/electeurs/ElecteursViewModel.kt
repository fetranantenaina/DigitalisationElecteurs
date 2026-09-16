package mg.example.electeurs

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EtatDetail(
    val electeur: Electeur? = null,
    val chargement: Boolean = true,
)

// Classe utilitaire pour combiner 4 flows dans le ViewModel
data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

class ElecteursViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ElecteurDatabase.obtenir(application).electeurDao()

    // États des filtres
    private val _recherche = MutableStateFlow("")
    val recherche: StateFlow<String> = _recherche.asStateFlow()

    private val _filtreRegion = MutableStateFlow<String?>(null)
    val filtreRegion: StateFlow<String?> = _filtreRegion.asStateFlow()

    private val _filtreDistrict = MutableStateFlow<String?>(null)
    val filtreDistrict: StateFlow<String?> = _filtreDistrict.asStateFlow()

    private val _filtreSexe = MutableStateFlow<String?>(null)
    val filtreSexe: StateFlow<String?> = _filtreSexe.asStateFlow()

    // Listes de choix disponibles pour les menus déroulants
    var listRegions by mutableStateOf<List<String>>(emptyList())
        private set
    var listDistricts by mutableStateOf<List<String>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            if (dao.parId(1L) == null) {
                dao.insererTous(electeursInitiaux)
            }
            rafraichirListesFiltres()
        }
    }

    private suspend fun rafraichirListesFiltres() {
        listRegions = dao.obtenirRegions()
        listDistricts = dao.obtenirDistricts(_filtreRegion.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val electeurs: StateFlow<List<Electeur>> = combine(
        _recherche,
        _filtreRegion,
        _filtreDistrict,
        _filtreSexe
    ) { texte, region, district, sexe ->
        Quad(texte, region, district, sexe)
    }.flatMapLatest { (texte, region, district, sexe) ->
        dao.filtrerElecteurs(
            text = texte.trim(),
            region = region,
            district = district,
            gender = sexe
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun rechercher(texte: String) {
        _recherche.value = texte
    }

    fun filtrerParRegion(region: String?) {
        _filtreRegion.value = region
        _filtreDistrict.value = null // Réinitialiser le district lié si la région change
        viewModelScope.launch {
            listDistricts = dao.obtenirDistricts(region)
        }
    }

    fun filtrerParDistrict(district: String?) {
        _filtreDistrict.value = district
    }

    fun filtrerParSexe(sexe: String?) {
        _filtreSexe.value = sexe
    }

    fun reinitialiserFiltres() {
        _recherche.value = ""
        _filtreRegion.value = null
        _filtreDistrict.value = null
        _filtreSexe.value = null
        viewModelScope.launch {
            listDistricts = dao.obtenirDistricts(null)
        }
    }

    fun ajouterElecteur(
        voterId: String,
        cin: String,
        name: String,
        birthDate: String,
        gender: String,
        region: String,
        district: String,
        fokontany: String,
        bureauVote: String,
        onResult: (succes: Boolean, erreur: String?) -> Unit,
    ) {
        val idNettoye = voterId.trim()
        val nomNettoye = name.trim()
        // On nettoie le CIN (enlève espaces éventuels saisis par l'utilisateur)
        val cinNettoye = cin.filter { it.isDigit() }
        val bureauNettoye = bureauVote.trim()

        if (idNettoye.isEmpty()) {
            onResult(false, "L'identifiant est obligatoire")
            return
        }
        if (nomNettoye.isEmpty()) {
            onResult(false, "Le nom est obligatoire")
            return
        }
        if (cinNettoye.length != 12) {
            onResult(false, "Le CIN doit contenir exactement 12 chiffres")
            return
        }
        if (bureauNettoye.isEmpty()) {
            onResult(false, "Le bureau de vote est obligatoire")
            return
        }

        viewModelScope.launch {
            if (dao.voterIdExiste(idNettoye)) {
                onResult(false, "Un électeur possède déjà cet identifiant")
                return@launch
            }
            if (dao.cinExiste(cinNettoye)) {
                onResult(false, "Un électeur possède déjà ce CIN")
                return@launch
            }

            val electeur = Electeur(
                voterId = idNettoye,
                cin = cinNettoye,
                name = nomNettoye,
                birthDate = birthDate.trim(),
                gender = gender.trim(),
                region = region.trim(),
                district = district.trim(),
                fokontany = fokontany.trim(),
                bureauVote = bureauNettoye,
            )

            val resultat = dao.inserer(electeur)
            if (resultat != -1L) {
                rafraichirListesFiltres()
                onResult(true, null)
            } else {
                onResult(false, "Impossible d'enregistrer l'électeur")
            }
        }
    }

    suspend fun chargerElecteur(id: Long): Electeur? =
        withContext(Dispatchers.IO) { dao.parId(id) }

    fun supprimerElecteur(id: Long, apresSuppression: () -> Unit) {
        viewModelScope.launch {
            dao.supprimer(id)
            rafraichirListesFiltres()
            apresSuppression()
        }
    }
}