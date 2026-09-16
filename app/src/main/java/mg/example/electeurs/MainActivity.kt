package mg.example.electeurs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material3.rememberDrawerState
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
// Routes centralisées (pour éviter les fautes de frappe)
// ---------------------------------------------------------------------------
object Routes {
    const val LISTE = "liste"
    const val AJOUT = "ajout"
    const val DETAIL = "detail" // + "/{electeurId}"
    fun detail(id: Long) = "detail/$id"
}

// ---------------------------------------------------------------------------
// Entrées du menu latéral
// ---------------------------------------------------------------------------
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

    // Titre dynamique selon l'écran courant
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
                            // Navigation "single top" : évite d'empiler plusieurs fois le même écran
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
                            // On retourne à la liste après un ajout réussi ou une annulation
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Barre de recherche globale
        OutlinedTextField(
            value = recherche,
            onValueChange = { viewModel.rechercher(it) },
            label = { Text("Recherche (Nom, CIN, ID, fokontany...)") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        // Filtres : Sexe & Région
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownFiltre(
                label = if (filtreSexe.isNullOrEmpty()) "Sexe : Tous" else "Sexe : $filtreSexe",
                options = listOf("Tous", "Homme", "Femme"),
                modifier = Modifier.weight(1f),
                onSelected = { viewModel.filtrerParSexe(if (it == "Tous") null else it) }
            )

            DropdownFiltre(
                label = if (filtreRegion.isNullOrEmpty()) "Région : Toutes" else "Région : $filtreRegion",
                options = listOf("Toutes") + viewModel.listRegions,
                modifier = Modifier.weight(1f),
                onSelected = { viewModel.filtrerParRegion(if (it == "Toutes") null else it) }
            )
        }
        Spacer(Modifier.height(6.dp))

        // Filtres : District & Réinitialisation
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownFiltre(
                label = if (filtreDistrict.isNullOrEmpty()) "District : Tous" else "District : $filtreDistrict",
                options = listOf("Tous") + viewModel.listDistricts,
                modifier = Modifier.weight(1f),
                onSelected = { viewModel.filtrerParDistrict(if (it == "Tous") null else it) }
            )

            Button(
                onClick = { viewModel.reinitialiserFiltres() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Effacer filtres")
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "${electeurs.size} électeur(s) trouvé(s)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))

        // ⚠️ Le bouton "Ajouter un électeur" a été retiré : il est désormais dans le menu latéral.

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
}

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
            Text(label, maxLines = 1, style = MaterialTheme.typography.bodySmall)
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
    var sexe by rememberSaveable { mutableStateOf("") }
    var region by rememberSaveable { mutableStateOf("") }
    var district by rememberSaveable { mutableStateOf("") }
    var fokontany by rememberSaveable { mutableStateOf("") }
    var bureauVote by rememberSaveable { mutableStateOf("") }
    var erreur by rememberSaveable { mutableStateOf<String?>(null) }

    // Pré-remplit le CIN avec le code postal de la région dès que la région est saisie
    LaunchedEffect(region) {
        val prefixe = codePostalDeRegion(region)
        if (region.isNotBlank() && (cin.isEmpty() || cin.length == 3)) {
            cin = prefixe
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text("Enregistrer un électeur", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

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
            supportingText = { Text("${cin.length}/12 chiffres • Préfixe région : ${codePostalDeRegion(region)}") },
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

        OutlinedTextField(
            value = naissance, onValueChange = { naissance = it },
            label = { Text("Date de naissance (JJ/MM/AAAA)") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = sexe, onValueChange = { sexe = it },
            label = { Text("Sexe") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = region, onValueChange = { region = it },
            label = { Text("Région") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = district, onValueChange = { district = it },
            label = { Text("District") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = fokontany, onValueChange = { fokontany = it },
            label = { Text("Fokontany") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = bureauVote, onValueChange = { bureauVote = it },
            label = { Text("Bureau de vote") },
            placeholder = { Text("EPP Analakely") },
            isError = erreur != null && bureauVote.isBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        erreur?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
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
                ) { succes, messageErreur ->
                    if (succes) onTermine() else erreur = messageErreur
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Enregistrer") }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = onTermine, modifier = Modifier.fillMaxWidth()) {
            Text("Annuler")
        }
    }
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