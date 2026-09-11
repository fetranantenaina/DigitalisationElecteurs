package mg.example.electeurs

data class Voter(
    val id: Long,
    val voterId: String,
    val name: String,
    val birthDate: String,
    val gender: String,
    val region: String,
    val district: String,
    val fokontany: String
)