package characters

import Operator
import com.example.*

// Classe responsável por validar as comparações entre operadores.
class OperatorValidator {

    // Função que valida a comparação entre dois operadores dados os seus nomes
    fun validateOperatorComparison(operatorChooseName: String?, operatorRandomName: String?): String {

        // Verifica se o nome do operador escolhido está nulo ou em branco
        if (operatorChooseName.isNullOrBlank()) {
            logger.warn("Missing parameter: choose is null or blank") // Loga um aviso de que o parâmetro está ausente
            return "{\"error\":\"Chosen operator is required.\"}" // Retorna uma mensagem de erro em formato JSON
        }

        // Busca o operador escolhido na lista de operadores, ignorando maiúsculas e minúsculas
        val operatorChoose = operatorsList.find { it.name.equals(operatorChooseName, ignoreCase = true) }

        // Busca o operador aleatório na lista de operadores, ignorando maiúsculas e minúsculas
        val operatorRandom = operatorsList.find { it.name.equals(operatorRandomName, ignoreCase = true) }

        // Verifica se algum dos operadores não foi encontrado
        if (operatorChoose == null || operatorRandom == null) {
            logger.warn("Operator not found: choose = $operatorChooseName") // Loga um aviso caso o operador escolhido não seja encontrado
            logger.warn("Operator not found: random = $operatorRandom") // Loga um aviso caso o operador aleatório não seja encontrado
            return "{\"error\":\"Operator not found.\"}" // Retorna uma mensagem de erro em formato JSON caso algum operador não tenha sido encontrado
        }

        // Compara os campos dos dois operadores e retorna a lista de comparações
        val fieldsToCompare = compareFields(operatorChoose, operatorRandom)

        // Compara o role dos operadores
        val roleComparison = compareRole(operatorChoose, operatorRandom)

        // Compara o ano de lançamento dos operadores e obtém um sinal (seta) indicando a direção da comparação
        val (releaseYearComparison, releaseYearArrow) = compareReleaseYear(operatorChoose, operatorRandom)

        // Cria a string de retorno (HTML) que contém as comparações dos campos e resultados
        val returnValue = buildString {
            // Para cada campo comparado, adiciona uma célula HTML com o valor e o estilo (verde ou vermelho)
            fieldsToCompare.forEach { (field, isMatch) ->
                // Obtém o valor do campo a partir do operador escolhido, usando reflexão para acessar o campo
                val value = operatorChoose::class.members.first { it.name == field }.call(operatorChoose)
                // Determina a classe CSS baseada na correspondência do campo (verde se coincidir, vermelho caso contrário)
                val cssClass = if (isMatch) "green" else "red"
                // Adiciona uma célula de comparação com o valor e a classe CSS correspondente
                append(generateComparisonCell(value.toString(), cssClass))
            }

            // Adiciona a célula comparando o ano de lançamento, com o sinal de direção (seta) incluído
            append(generateComparisonCell("${operatorChoose.release_year} $releaseYearArrow", releaseYearComparison))
            // Adiciona a célula comparando os papéis (roles) dos operadores
            append(generateComparisonCell(operatorChoose.role.joinToString(", "), roleComparison))
        }

        // Retorna o HTML gerado com todas as comparações
        return returnValue
    }

    // Função para gerar a comparação de campos entre dois operadores
    private fun compareFields(operatorChoose: Operator, operatorRandom: Operator): List<Pair<String, Boolean>> {
        return listOf(
            "name" to (operatorChoose.name == operatorRandom.name),
            "gender" to (operatorChoose.gender == operatorRandom.gender),
            "side" to (operatorChoose.side == operatorRandom.side),
            "country" to (operatorChoose.country == operatorRandom.country),
            "org" to (operatorChoose.org == operatorRandom.org),
            "squad" to (operatorChoose.squad == operatorRandom.squad)
        )
    }

    // Função para comparar o role dos operadores
    private fun compareRole(operatorChoose: Operator, operatorRandom: Operator): String {
        return when {
            operatorChoose.role == operatorRandom.role -> "green"
            operatorChoose.role.any { it in operatorRandom.role } -> "yellow"
            else -> "red"
        }
    }

    // Função para comparar o ano de lançamento
    private fun compareReleaseYear(operatorChoose: Operator, operatorRandom: Operator): Pair<String, String> {
        val comparison = when {
            operatorChoose.release_year == operatorRandom.release_year -> "green"
            operatorChoose.release_year < operatorRandom.release_year -> "red"
            else -> "red"
        }
        val arrow = when {
            operatorChoose.release_year < operatorRandom.release_year -> "↑"
            operatorChoose.release_year > operatorRandom.release_year -> "↓"
            else -> ""
        }
        return comparison to arrow
    }

    // Função para gerar a célula HTML com base na comparação
    private fun generateComparisonCell(value: String, cssClass: String): String {
        return """
            <div class="cell $cssClass">
                <strong>$value</strong>
            </div>
        """.trimIndent()
    }
}
