package mg.example.electeurs

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*

class MainActivity : Activity() {

    private lateinit var db: DatabaseHelper
    private lateinit var listView: ListView
    private lateinit var countText: TextView
    private var voters = listOf<Voter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)
        listView = findViewById(R.id.voterList)
        countText = findViewById(R.id.countText)

        findViewById<Button>(R.id.addButton).setOnClickListener {
            startActivity(Intent(this, AddVoterActivity::class.java))
        }

        findViewById<Button>(R.id.searchButton).setOnClickListener {
            showSearchDialog()
        }

        registerForContextMenu(listView)

        listView.setOnItemClickListener { _, _, position, _ ->
            val voter = voters[position]
            startActivity(
                Intent(this, VoterDetailActivity::class.java)
                    .putExtra("voter_id", voter.id)
            )
        }

        refreshList()
    }

    override fun onResume() {
        super.onResume()
        if (::db.isInitialized) refreshList()
    }

    private fun refreshList() {
        voters = db.getAllVoters()
        updateList(voters)
    }

    private fun updateList(data: List<Voter>) {
        countText.text = "${data.size} électeur(s) enregistré(s)"
        // Utilisation d'un ArrayAdapter personnalisé pour gérer les deux lignes de simple_list_item_2
        listView.adapter = object : ArrayAdapter<Voter>(
            this, android.R.layout.simple_list_item_2, android.R.id.text1, data
        ) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val item = getItem(position)!!
                view.findViewById<TextView>(android.R.id.text1).text = item.name
                view.findViewById<TextView>(android.R.id.text2).text =
                    "ID: ${item.voterId} • ${item.region} / ${item.district}"
                return view
            }
        }
    }

    private fun showSearchDialog() {
        val input = EditText(this)
        input.hint = "Nom, ID, région, district..."
        AlertDialog.Builder(this)
            .setTitle("Rechercher un électeur")
            .setView(input)
            .setPositiveButton("Rechercher") { _, _ ->
                updateList(db.searchVoters(input.text.toString().trim()))
            }
            .setNeutralButton("Tout afficher") { _, _ ->
                refreshList()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("Actualiser")
        menu.add("À propos")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.title.toString()) {
            "Actualiser" -> { refreshList(); true }
            "À propos" -> {
                AlertDialog.Builder(this)
                    .setTitle("Électeurs Malagasy")
                    .setMessage("Prototype pédagogique de digitalisation et d'affichage de données électorales locales.\n\nLes données sont stockées uniquement sur l'appareil dans SQLite.")
                    .setPositiveButton("OK", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add("Afficher")
        menu.add("Supprimer")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val voter = voters[info.position]

        return when (item.title.toString()) {
            "Afficher" -> {
                startActivity(Intent(this, VoterDetailActivity::class.java)
                    .putExtra("voter_id", voter.id))
                true
            }
            "Supprimer" -> {
                AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Supprimer la fiche de ${voter.name} ?")
                    .setPositiveButton("Supprimer") { _, _ ->
                        db.deleteVoter(voter.id)
                        refreshList()
                        Toast.makeText(this, "Fiche supprimée", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Annuler", null)
                    .show()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }
}
