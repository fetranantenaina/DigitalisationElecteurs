package mg.example.electeurs

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class VoterDetailActivity : Activity() {

    private lateinit var db: DatabaseHelper
    private var voterId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voter_detail)

        db = DatabaseHelper(this)
        voterId = intent.getLongExtra("voter_id", -1L)

        val voter = db.getVoter(voterId)
        if (voter == null) {
            Toast.makeText(this, "Électeur introuvable", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<TextView>(R.id.detailText).text = """
            Identifiant : ${voter.voterId}

            Nom complet : ${voter.name}
            Date de naissance : ${voter.birthDate}
            Sexe : ${voter.gender}

            Région : ${voter.region}
            District : ${voter.district}
            Fokontany : ${voter.fokontany}
        """.trimIndent()

        findViewById<Button>(R.id.deleteButton).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Supprimer")
                .setMessage("Voulez-vous supprimer cette fiche ?")
                .setPositiveButton("Supprimer") { _, _ ->
                    db.deleteVoter(voterId)
                    Toast.makeText(this, "Fiche supprimée", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }
}
