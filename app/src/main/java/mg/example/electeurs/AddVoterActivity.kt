package mg.example.electeurs

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class AddVoterActivity : Activity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_voter)

        db = DatabaseHelper(this)

        val cinInput = findViewById<EditText>(
            R.id.cinInput
        )

        val nameInput = findViewById<EditText>(
            R.id.nameInput
        )

        val birthInput = findViewById<EditText>(
            R.id.birthInput
        )

        val genderInput = findViewById<EditText>(
            R.id.genderInput
        )

        val regionInput = findViewById<EditText>(
            R.id.regionInput
        )

        val districtInput = findViewById<EditText>(
            R.id.districtInput
        )

        val fokontanyInput = findViewById<EditText>(
            R.id.fokontanyInput
        )

        val saveButton = findViewById<Button>(
            R.id.saveButton
        )

        val cancelButton = findViewById<Button>(
            R.id.cancelButton
        )

        /*
         * Bouton ENREGISTRER
         */
        saveButton.setOnClickListener {

            val voterId = cinInput.text
                .toString()
                .trim()

            val name = nameInput.text
                .toString()
                .trim()

            val birthDate = birthInput.text
                .toString()
                .trim()

            val gender = genderInput.text
                .toString()
                .trim()

            val region = regionInput.text
                .toString()
                .trim()

            val district = districtInput.text
                .toString()
                .trim()

            val fokontany = fokontanyInput.text
                .toString()
                .trim()

            /*
             * Vérification de l'identifiant
             */
            if (voterId.isEmpty()) {

                cinInput.error =
                    "L'identifiant est obligatoire"

                cinInput.requestFocus()

                Toast.makeText(
                    this,
                    "Veuillez saisir l'identifiant de l'électeur",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            /*
             * Vérification du nom
             */
            if (name.isEmpty()) {

                nameInput.error =
                    "Le nom est obligatoire"

                nameInput.requestFocus()

                Toast.makeText(
                    this,
                    "Veuillez saisir le nom de l'électeur",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            /*
             * Vérification d'un identifiant déjà utilisé
             */
            if (db.voterIdExists(voterId)) {

                cinInput.error =
                    "Cet identifiant existe déjà"

                cinInput.requestFocus()

                Toast.makeText(
                    this,
                    "Un électeur possède déjà cet identifiant",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            /*
             * Création de l'objet électeur
             */
            val voter = Voter(

                id = 0,

                voterId = voterId,

                name = name,

                birthDate = birthDate,

                gender = gender,

                region = region,

                district = district,

                fokontany = fokontany
            )

            /*
             * Insertion SQLite
             */
            val result = db.insertVoter(voter)

            if (result != -1L) {

                Toast.makeText(
                    this,
                    "Électeur enregistré avec succès",
                    Toast.LENGTH_SHORT
                ).show()

                /*
                 * Retour à MainActivity
                 */
                finish()

            } else {

                Toast.makeText(
                    this,
                    "Impossible d'enregistrer l'électeur",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        /*
         * Bouton ANNULER
         */
        cancelButton.setOnClickListener {

            finish()
        }
    }
}