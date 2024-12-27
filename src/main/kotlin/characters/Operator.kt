import kotlinx.serialization.Serializable

@Serializable
data class Operator(
    val name: String,
    val icon: String,
    val gender: String,
    val role: List<String>,
    val side: String,
    val country: String,
    val Org: String,
    val Squad: String,
    val release_year: String
)
