import kotlinx.serialization.Serializable

@Serializable
data class Operator(
    val name: String,
    val icon: String,
    val gender: String,
    val role: List<String>,
    val side: String,
    val country: String,
    val org: String,
    val squad: String,
    val release_year: String
)
