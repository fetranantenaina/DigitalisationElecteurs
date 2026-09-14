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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val viewModel: ElecteursViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "liste",
        modifier = modifier,
    ) {
        composable("liste") {
            EcranListe(
                viewModel = viewModel,
                onAjouterClick = { navController.navigate("ajout") },
                onVoterClick = { id -> navController.navigate("detail/$id") },
            )
        }

        composable("ajout") {
            EcranAjout(
                viewModel = viewModel,
                onTermine = { navController.popBackStack() },
            )
        }

        composable("detail/{voterId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("voterId")?.toLongOrNull() ?: -1L
            EcranDetail(
                viewModel = viewModel,
                voterId = id,
                onRetour = { navController.popBackStack() },
            )
        }
    }
}

@Composable
fun EcranListe(
    viewModel: ElecteursViewModel,
    onAjouterClick: () -> Unit,
    onVoterClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val voters by viewModel.voters.collectAsState()
    val recherche by viewModel.recherche.collectAsState()
    val filtreRegion by viewModel.filtreRegion.collectAsState()
    val filtreDistrict by viewModel.filtreDistrict.collectAsState()
    val filtreSexe by viewModel.filtreSexe.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("Digitalisation des électeurs (Room)", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        // Barre de recherche globale
        OutlinedTextField(
            value = recherche,
            onValueChange = { viewModel.rechercher(it) },
            label = { Text("Recherche globale (Nom, ID, fokontany...)") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        // Section des filtres par colonnes (Sexe & Région)
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

        // Section des filtres (District & Bouton de réinitialisation)
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
            "${voters.size} électeur(s) trouvé(s)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))

        Button(onClick = onAjouterClick, modifier = Modifier.fillMaxWidth()) {
            Text("Ajouter un électeur")
        }
        Spacer(Modifier.height(12.dp))

        LazyColumn {
            items(voters, key = { it.id }) { voter ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                ) {
                    ListItem(
                        headlineContent = { Text("${voter.name} (${voter.gender})") },
                        supportingContent = {
                            Text("ID: ${voter.voterId} • Région: ${voter.region} • Dist: ${voter.district}")
                        },
                        modifier = Modifier.padding(4.dp),
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { onVoterClick(voter.id) }) {
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
    // rememberSaveable : ces champs survivent maintenant à la rotation de l'écran
    // et aux recréations de l'Activity (contrairement à remember seul).
    var cin by rememberSaveable { mutableStateOf("") }
    var nom by rememberSaveable { mutableStateOf("") }
    var naissance by rememberSaveable { mutableStateOf("") }
    var sexe by rememberSaveable { mutableStateOf("") }
    var region by rememberSaveable { mutableStateOf("") }
    var district by rememberSaveable { mutableStateOf("") }
    var fokontany by rememberSaveable { mutableStateOf("") }
    var erreur by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text("Enregistrer un électeur", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = cin, onValueChange = { cin = it },
            label = { Text("Numéro électeur / identifiant") },
            isError = erreur != null && cin.isBlank(),
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

        erreur?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                viewModel.ajouterElecteur(
                    voterId = cin,
                    name = nom,
                    birthDate = naissance,
                    gender = sexe,
                    region = region,
                    district = district,
                    fokontany = fokontany,
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
    voterId: Long,
    onRetour: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var etat by remember { mutableStateOf(EtatDetail()) }
    var confirmationSuppression by remember { mutableStateOf(false) }

    LaunchedEffect(voterId) {
        val voter = viewModel.chargerElecteur(voterId)
        etat = EtatDetail(voter = voter, chargement = false)
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
            etat.voter == null -> Text("Électeur introuvable")
            else -> {
                val voter = etat.voter!!
                Text(voter.name, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))
                Text("Identifiant : ${voter.voterId}")
                Text("Date de naissance : ${voter.birthDate}")
                Text("Sexe : ${voter.gender}")
                Spacer(Modifier.height(8.dp))
                Text("Région : ${voter.region}")
                Text("District : ${voter.district}")
                Text("Fokontany : ${voter.fokontany}")
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
                            viewModel.supprimerElecteur(voterId, apresSuppression = onRetour)
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