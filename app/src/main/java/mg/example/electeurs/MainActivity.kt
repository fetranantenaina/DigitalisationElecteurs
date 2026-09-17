package mg.example.electeurs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Routes centralisées
// ---------------------------------------------------------------------------
object Routes {
    const val LISTE = "liste"
    const val AJOUT = "ajout"
    const val DETAIL = "detail"
    fun detail(id: Long) = "detail/$id"
}

private data class EntreeMenu(
    val route: String,
    val libelle: String,
    val icone: @Composable () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val viewModel: ElecteursViewModel = viewModel()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val routeCourante = backStackEntry?.destination?.route ?: Routes.LISTE
    val titre = when {
        routeCourante.startsWith(Routes.AJOUT) -> "Ajouter un électeur"
        routeCourante.startsWith(Routes.DETAIL) -> "Fiche électeur"
        else -> "Liste des électeurs"
    }

    val entrees = listOf(
        EntreeMenu(Routes.LISTE, "Liste des électeurs") {
            Icon(Icons.Filled.List, contentDescription = null)
        },
        EntreeMenu(Routes.AJOUT, "Ajouter un électeur") {
            Icon(Icons.Filled.Add, contentDescription = null)
        },
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Digitalisation des électeurs",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                entrees.forEach { entree ->
                    NavigationDrawerItem(
                        icon = entree.icone,
                        label = { Text(entree.libelle) },
                        selected = routeCourante == entree.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(entree.route) {
                                popUpTo(Routes.LISTE) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        },
        modifier = modifier,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(titre) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Ouvrir le menu")
                        }
                    },
                )
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.LISTE,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(Routes.LISTE) {
                    EcranListe(
                        viewModel = viewModel,
                        onElecteurClick = { id -> navController.navigate(Routes.detail(id)) },
                    )
                }

                composable(Routes.AJOUT) {
                    EcranAjout(
                        viewModel = viewModel,
                        onTermine = {
                            navController.navigate(Routes.LISTE) {
                                popUpTo(Routes.LISTE) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                    )
                }

                composable("${Routes.DETAIL}/{electeurId}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("electeurId")?.toLongOrNull() ?: -1L
                    EcranDetail(
                        viewModel = viewModel,
                        electeurId = id,
                        onRetour = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ÉCRAN LISTE — épuré, avec un bouton qui ouvre le bottom sheet de filtres
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcranListe(
    viewModel: ElecteursViewModel,
    onElecteurClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val electeurs by viewModel.electeurs.collectAsState()
    val recherche by viewModel.recherche.collectAsState()
    val filtreRegion by viewModel.filtreRegion.collectAsState()
    val filtreDistrict by viewModel.filtreDistrict.collectAsState()
    val filtreSexe by viewModel.filtreSexe.collectAsState()
    val tri by viewModel.tri.collectAsState()

    // État d'ouverture du panneau de recherche/filtres
    var afficherFiltres by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // Drapeaux individuels pour savoir ce qui est actif
    val aUnFiltre = recherche.isNotBlank() ||
            !filtreRegion.isNullOrEmpty() ||
            !filtreDistrict.isNullOrEmpty() ||
            !filtreSexe.isNullOrEmpty()
    val aUnTri = tri != TriElecteur.NOM_ASC

    // Nombre total d'éléments actifs (filtres + tri non par défaut)
    val nbFiltresActifs = listOf(
        recherche.isNotBlank(),
        !filtreRegion.isNullOrEmpty(),
        !filtreDistrict.isNullOrEmpty(),
        !filtreSexe.isNullOrEmpty(),
        aUnTri,
    ).count { it }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // ─── Barre d'action : bouton Rechercher / Filtrer ───
        Button(
            onClick = { afficherFiltres = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Filled.Search, contentDescription = null)
            Text(
                text = if (nbFiltresActifs == 0) "Rechercher / Filtrer"
                else "Rechercher / Filtrer ($nbFiltresActifs)",
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        Spacer(Modifier.height(8.dp))

        // ─── Rappel compact : filtres actifs ET tri courant ───
        if (aUnFiltre) {
            Text(
                text = buildString {
                    if (recherche.isNotBlank()) append("« $recherche » ")
                    if (!filtreRegion.isNullOrEmpty()) append("• $filtreRegion ")
                    if (!filtreDistrict.isNullOrEmpty()) append("• $filtreDistrict ")
                    if (!filtreSexe.isNullOrEmpty()) append("• $filtreSexe")
                }.trim(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
        }
        if (aUnTri) {
            Text(
                text = "Tri : ${tri.libelle}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
        }

        Text(
            "${electeurs.size} électeur(s) trouvé(s)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))

        // ─── Liste des électeurs ───
        LazyColumn {
            items(electeurs, key = { it.id }) { electeur ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                ) {
                    ListItem(
                        headlineContent = { Text("${electeur.name} (${electeur.gender})") },
                        supportingContent = {
                            Text("CIN: ${formaterCin(electeur.cin)} • ID: ${electeur.voterId}")
                            Text("Région: ${electeur.region} • Bureau: ${electeur.bureauVote}")
                        },
                        modifier = Modifier.padding(4.dp),
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { onElecteurClick(electeur.id) }) {
                            Text("Voir la fiche")
                        }
                    }
                }
            }
        }
    }

    // ─── Bottom sheet de recherche, filtres et tri ───
    if (afficherFiltres) {
        ModalBottomSheet(
            onDismissRequest = { afficherFiltres = false },
            sheetState = sheetState,
        ) {
            PanneauRechercheFiltres(
                viewModel = viewModel,
                onFermer = { afficherFiltres = false },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// CONTENU DU BOTTOM SHEET — recherche + filtres + tri
// ---------------------------------------------------------------------------
@Composable
fun PanneauRechercheFiltres(
    viewModel: ElecteursViewModel,
    onFermer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val recherche by viewModel.recherche.collectAsState()
    val filtreRegion by viewModel.filtreRegion.collectAsState()
    val filtreDistrict by viewModel.filtreDistrict.collectAsState()
    val filtreSexe by viewModel.filtreSexe.collectAsState()
    val tri by viewModel.tri.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
    ) {
        Text(
            "Rechercher et filtrer",
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Affinez et triez la liste des électeurs selon vos critères.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        // ─── Recherche texte ───
        OutlinedTextField(
            value = recherche,
            onValueChange = { viewModel.rechercher(it) },
            label = { Text("Recherche (Nom, CIN, ID, fokontany...)") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))

        // ─── Sexe ───
        DropdownFiltre(
            label = if (filtreSexe.isNullOrEmpty()) "Sexe : Tous" else "Sexe : $filtreSexe",
            options = listOf("Tous", "Homme", "Femme"),
            modifier = Modifier.fillMaxWidth(),
            onSelected = { viewModel.filtrerParSexe(if (it == "Tous") null else it) }
        )
        Spacer(Modifier.height(10.dp))

        // ─── Région ───
        DropdownFiltre(
            label = if (filtreRegion.isNullOrEmpty()) "Région : Toutes" else "Région : $filtreRegion",
            options = listOf("Toutes") + viewModel.listRegions,
            modifier = Modifier.fillMaxWidth(),
            onSelected = { viewModel.filtrerParRegion(if (it == "Toutes") null else it) }
        )
        Spacer(Modifier.height(10.dp))

        // ─── District ───
        DropdownFiltre(
            label = if (filtreDistrict.isNullOrEmpty()) "District : Tous" else "District : $filtreDistrict",
            options = listOf("Tous") + viewModel.listDistricts,
            modifier = Modifier.fillMaxWidth(),
            onSelected = { viewModel.filtrerParDistrict(if (it == "Tous") null else it) }
        )
        Spacer(Modifier.height(16.dp))

        // ─── Tri ───
        Text(
            "Trier par",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))

        DropdownFiltre(
            label = "Tri : ${tri.libelle}",
            options = TriElecteur.entries.map { it.libelle },
            modifier = Modifier.fillMaxWidth(),
            onSelected = { libelle ->
                TriElecteur.entries.firstOrNull { it.libelle == libelle }
                    ?.let { viewModel.changerTri(it) }
            }
        )

        Spacer(Modifier.height(20.dp))

        // ─── Actions ───
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = { viewModel.reinitialiserFiltres() },
                modifier = Modifier.weight(1f),
            ) {
                Text("Réinitialiser")
            }
            Button(
                onClick = onFermer,
                modifier = Modifier.weight(1f),
            ) {
                Text("Voir les résultats")
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Menu déroulant générique utilisé pour les filtres
// ---------------------------------------------------------------------------
@Composable
fun DropdownFiltre(
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(label, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    }
                )
            }
        }
    }
}

/**
 * Composant générique : un vrai "select" Material 3.
 * Utilisé pour Sexe, Région, District, Fokontany et Bureau de vote.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelection(
    label: String,
    valeur: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = valeur,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcranAjout(
    viewModel: ElecteursViewModel,
    onTermine: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var cin by rememberSaveable { mutableStateOf("") }
    var idElecteur by rememberSaveable { mutableStateOf("") }
    var nom by rememberSaveable { mutableStateOf("") }
    var naissance by rememberSaveable { mutableStateOf("") }
    var afficherSelecteurDate by rememberSaveable { mutableStateOf(false) }
    var sexe by rememberSaveable { mutableStateOf("") }
    var region by rememberSaveable { mutableStateOf("") }
    var district by rememberSaveable { mutableStateOf("") }
    var fokontany by rememberSaveable { mutableStateOf("") }
    var bureauVote by rememberSaveable { mutableStateOf("") }
    var erreur by rememberSaveable { mutableStateOf<String?>(null) }

    val libellesEtapes = listOf("Identité", "Localisation")
    var etape by rememberSaveable { mutableStateOf(0) }
    val derniereEtape = libellesEtapes.lastIndex

    LaunchedEffect(region) {
        val prefixe = codePostalDeRegion(region)
        if (region.isNotBlank() && (cin.isEmpty() || cin.length == 3)) {
            cin = prefixe
        }
    }

    LaunchedEffect(region) {
        if (district.isNotBlank() && !districtsDeRegion(region).contains(district)) {
            district = ""
            fokontany = ""
            bureauVote = ""
        }
    }

    LaunchedEffect(district) {
        if (fokontany.isNotBlank() &&
            !fokontanyDeDistrict(region, district).contains(fokontany)
        ) {
            fokontany = ""
            bureauVote = ""
        }
    }

    LaunchedEffect(fokontany) {
        if (bureauVote.isNotBlank() &&
            !bureauxDeFokontany(region, district, fokontany).contains(bureauVote)
        ) {
            bureauVote = ""
        }
    }

    val districtsDisponibles = remember(region) { districtsDeRegion(region) }
    val fokontanyDisponibles = remember(region, district) { fokontanyDeDistrict(region, district) }
    val bureauxDisponibles = remember(region, district, fokontany) {
        bureauxDeFokontany(region, district, fokontany)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text("Enregistrer un électeur", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        IndicateurEtapes(etapeActuelle = etape, libelles = libellesEtapes)
        Spacer(Modifier.height(20.dp))

        when (etape) {
            0 -> {
                OutlinedTextField(
                    value = idElecteur, onValueChange = { idElecteur = it },
                    label = { Text("Numéro électeur / identifiant") },
                    isError = erreur != null && idElecteur.isBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = formaterCin(cin),
                    onValueChange = { nouvelle ->
                        cin = nouvelle.filter { it.isDigit() }.take(12)
                    },
                    label = { Text("CIN (12 chiffres)") },
                    placeholder = { Text("101 234 567 890") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = erreur != null && cin.length != 12,
                    supportingText = {
                        Text("${cin.length}/12 chiffres • Préfixe région : ${codePostalDeRegion(region)}")
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = nom, onValueChange = { nom = it },
                    label = { Text("Nom complet") },
                    isError = erreur != null && nom.isBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))

                val interactionSourceDate = remember { MutableInteractionSource() }
                val champDateAppuye by interactionSourceDate.collectIsPressedAsState()
                LaunchedEffect(champDateAppuye) {
                    if (champDateAppuye) afficherSelecteurDate = true
                }

                OutlinedTextField(
                    value = naissance,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date de naissance") },
                    placeholder = { Text("JJ/MM/AAAA") },
                    isError = erreur != null && naissance.isBlank(),
                    trailingIcon = {
                        IconButton(onClick = { afficherSelecteurDate = true }) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Choisir une date")
                        }
                    },
                    interactionSource = interactionSourceDate,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))

                if (afficherSelecteurDate) {
                    val etatSelecteurDate = rememberDatePickerState(
                        initialSelectedDateMillis = System.currentTimeMillis(),
                    )
                    DatePickerDialog(
                        onDismissRequest = { afficherSelecteurDate = false },
                        confirmButton = {
                            TextButton(onClick = {
                                etatSelecteurDate.selectedDateMillis?.let { millis ->
                                    naissance = formaterDateDepuisMillis(millis)
                                }
                                afficherSelecteurDate = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { afficherSelecteurDate = false }) { Text("Annuler") }
                        },
                    ) {
                        DatePicker(state = etatSelecteurDate)
                    }
                }

                DropdownSelection(
                    label = "Sexe",
                    valeur = sexe.ifBlank { "Sélectionner..." },
                    options = listOf("Homme", "Femme"),
                    onSelected = { sexe = it },
                )
                Spacer(Modifier.height(8.dp))
            }

            1 -> {
                DropdownSelection(
                    label = "Région",
                    valeur = region.ifBlank { "Sélectionner..." },
                    options = NOMS_REGIONS,
                    onSelected = { region = it },
                )
                Spacer(Modifier.height(8.dp))

                DropdownSelection(
                    label = "District",
                    valeur = district.ifBlank {
                        if (region.isBlank()) "Choisir d'abord une région" else "Sélectionner..."
                    },
                    options = districtsDisponibles,
                    enabled = region.isNotBlank(),
                    onSelected = { district = it },
                )
                Spacer(Modifier.height(8.dp))

                DropdownSelection(
                    label = "Fokontany",
                    valeur = fokontany.ifBlank {
                        if (district.isBlank()) "Choisir d'abord un district" else "Sélectionner..."
                    },
                    options = fokontanyDisponibles,
                    enabled = district.isNotBlank(),
                    onSelected = { fokontany = it },
                )
                Spacer(Modifier.height(8.dp))

                DropdownSelection(
                    label = "Bureau de vote",
                    valeur = bureauVote.ifBlank {
                        if (fokontany.isBlank()) "Choisir d'abord un fokontany" else "Sélectionner..."
                    },
                    options = bureauxDisponibles,
                    enabled = fokontany.isNotBlank(),
                    onSelected = { bureauVote = it },
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        erreur?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (etape > 0) {
                OutlinedButton(
                    onClick = {
                        erreur = null
                        etape -= 1
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Précédent") }
            }

            if (etape < derniereEtape) {
                Button(
                    onClick = {
                        val messageErreur = validerEtapeIdentite(idElecteur, cin, nom, naissance, sexe)
                        if (messageErreur != null) {
                            erreur = messageErreur
                        } else {
                            erreur = null
                            etape += 1
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Suivant") }
            } else {
                Button(
                    onClick = {
                        val messageErreur = validerEtapeLocalisation(region, district, fokontany, bureauVote)
                        if (messageErreur != null) {
                            erreur = messageErreur
                            return@Button
                        }
                        viewModel.ajouterElecteur(
                            voterId = idElecteur,
                            cin = cin,
                            name = nom,
                            birthDate = naissance,
                            gender = sexe,
                            region = region,
                            district = district,
                            fokontany = fokontany,
                            bureauVote = bureauVote,
                        ) { succes, erreurRetour ->
                            if (succes) onTermine() else erreur = erreurRetour
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Terminé") }
            }
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = onTermine, modifier = Modifier.fillMaxWidth()) {
            Text("Annuler")
        }
    }
}

@Composable
private fun IndicateurEtapes(etapeActuelle: Int, libelles: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        libelles.forEachIndexed { index, libelle ->
            val estTerminee = index < etapeActuelle
            val estActive = index == etapeActuelle
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                val couleurFond = when {
                    estTerminee || estActive -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val couleurTexte = when {
                    estTerminee || estActive -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .padding(horizontal = 2.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = couleurFond,
                        modifier = Modifier.height(28.dp),
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center,
                        ) {
                            if (estTerminee) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "Étape terminée",
                                    tint = couleurTexte,
                                    modifier = Modifier.height(16.dp),
                                )
                            } else {
                                Text("${index + 1}", color = couleurTexte)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    libelle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (estActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun validerEtapeIdentite(
    idElecteur: String,
    cin: String,
    nom: String,
    naissance: String,
    sexe: String,
): String? = when {
    idElecteur.isBlank() -> "L'identifiant est obligatoire"
    nom.isBlank() -> "Le nom est obligatoire"
    cin.length != 12 -> "Le CIN doit contenir exactement 12 chiffres"
    naissance.isBlank() -> "La date de naissance est obligatoire"
    sexe.isBlank() -> "Le sexe est obligatoire"
    else -> null
}

private fun validerEtapeLocalisation(
    region: String,
    district: String,
    fokontany: String,
    bureauVote: String,
): String? = when {
    region.isBlank() -> "La région est obligatoire"
    district.isBlank() -> "Le district est obligatoire"
    fokontany.isBlank() -> "Le fokontany est obligatoire"
    bureauVote.isBlank() -> "Le bureau de vote est obligatoire"
    else -> null
}

@Composable
fun EcranDetail(
    viewModel: ElecteursViewModel,
    electeurId: Long,
    onRetour: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var etat by remember { mutableStateOf(EtatDetail()) }
    var confirmationSuppression by remember { mutableStateOf(false) }

    LaunchedEffect(electeurId) {
        val electeur = viewModel.chargerElecteur(electeurId)
        etat = EtatDetail(electeur = electeur, chargement = false)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text("Fiche électeur", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        when {
            etat.chargement -> Text("Chargement...")
            etat.electeur == null -> Text("Électeur introuvable")
            else -> {
                val electeur = etat.electeur!!
                Text(electeur.name, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))
                Text("Identifiant : ${electeur.voterId}")
                Text("CIN : ${formaterCin(electeur.cin)}")
                Text("Date de naissance : ${electeur.birthDate}")
                Text("Sexe : ${electeur.gender}")
                Spacer(Modifier.height(8.dp))
                Text("Région : ${electeur.region}")
                Text("District : ${electeur.district}")
                Text("Fokontany : ${electeur.fokontany}")
                Text("Bureau de vote : ${electeur.bureauVote}")
                Spacer(Modifier.height(24.dp))

                if (confirmationSuppression) {
                    Text(
                        "Supprimer cette fiche ? Cette action est irréversible.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            confirmationSuppression = false
                            viewModel.supprimerElecteur(electeurId, apresSuppression = onRetour)
                        }) { Text("Confirmer") }
                        TextButton(onClick = { confirmationSuppression = false }) {
                            Text("Annuler")
                        }
                    }
                } else {
                    Button(onClick = onRetour, modifier = Modifier.fillMaxWidth()) {
                        Text("Retour à la liste")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { confirmationSuppression = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Supprimer cette fiche") }
                }
            }
        }
    }
}

private fun formaterDateDepuisMillis(millis: Long): String {
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(Date(millis))
}