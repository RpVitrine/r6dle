package com.example

import Operator
import characters.OperatorValidator
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import io.ktor.server.plugins.cors.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("ValidateFieldsEndpoint")
val operatorsList = mutableListOf<Operator>()
val selectedOperators = mutableSetOf<String>() // Lista para armazenar operadores já escolhidos

// Função para carregar operadores
fun loadOperators() {
    val jsonFile = File("src/main/resources/characters/characters.json")
    if (jsonFile.exists()) {
        val jsonData = jsonFile.readText()
        operatorsList.addAll(Json.decodeFromString(jsonData))
    } else {
        logger.error("JSON file not found at path: src/main/resources/characters/characters.json")
    }
}

// Configuração de Roteamento e API
fun Application.configureRouting() {
    loadOperators()

    install(CORS) {
        allowHost("localhost:8080", schemes = listOf("http"))
        allowHost("127.0.0.1:8080", schemes = listOf("http"))

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post) // Se você estiver usando POST também
    }

    routing {
        static("/") {
            resources("static")
            defaultResource("index.html", "static")
        }

        get("/") {
            call.respondFile(File("src/main/resources/static/index.html"))
        }

        static("/resources") {
            resources("/")
        }

        get("/api/random-operator") {
            // Verifica se o referer está presente e se é válido (localhost)
            val referer = call.request.headers[HttpHeaders.Referrer]
            if (referer == null ||
                !(referer.startsWith("http://localhost") || referer.startsWith("http://127.0.0.1"))) {
                call.respond(HttpStatusCode.Forbidden, "{\"error\":\"Access denied. Invalid or missing Referer.\"}")
                return@get
            }

            if (operatorsList.isEmpty()) {
                call.respond(HttpStatusCode.InternalServerError, "{\"error\":\"No operators available.\"}")
                return@get
            }

            val randomOperator = operatorsList.random()
            val jsonResponse = Json.encodeToString(randomOperator.name)
            call.respond(HttpStatusCode.OK, jsonResponse)
        }

        get("/api/all-operator-name") {
            // Verifica se o referer está presente e se é válido (localhost)
            val referer = call.request.headers[HttpHeaders.Referrer]
            if (referer == null ||
                !(referer.startsWith("http://localhost") || referer.startsWith("http://127.0.0.1"))) {
                call.respond(HttpStatusCode.Forbidden, "{\"error\":\"Access denied. Invalid or missing Referer.\"}")
                return@get
            }

            val resourceBasePath = "/resources/"
            val operatorData = operatorsList.map { operator ->
                mapOf(
                    "name" to operator.name,
                    "icon" to resourceBasePath + operator.icon
                )
            }
            val jsonResponse = Json.encodeToString(operatorData)
            call.respond(HttpStatusCode.OK, jsonResponse)
        }

        get("/api/restart-game"){
            // Verifica se o referer está presente e se é válido (localhost)
            val referer = call.request.headers[HttpHeaders.Referrer]
            if (referer == null ||
                !(referer.startsWith("http://localhost") || referer.startsWith("http://127.0.0.1"))) {
                call.respond(HttpStatusCode.Forbidden, "{\"error\":\"Access denied. Invalid or missing Referer.\"}")
                return@get
            }

            selectedOperators.clear()
            call.respond(HttpStatusCode.OK, "{\"message\":\"Game restarted successfully.\"}")
        }

        get("/api/validate-all-fields") {
            // Verifica se o referer está presente e se é válido (localhost)
            val referer = call.request.headers[HttpHeaders.Referrer]
            if (referer == null ||
                !(referer.startsWith("http://localhost") || referer.startsWith("http://127.0.0.1"))) {
                call.respond(HttpStatusCode.Forbidden, "{\"error\":\"Access denied. Invalid or missing Referer.\"}")
                return@get
            }

            val operatorChooseName = call.request.queryParameters["choose"]
            val operatorRandomName = call.request.queryParameters["random"]

            logger.info("Received request with chosen operator: $operatorChooseName")
            logger.info("Received request with random operator: $operatorRandomName")

            // Verificar se o operador escolhido já foi selecionado
            if (operatorChooseName != null && selectedOperators.contains(operatorChooseName)) {
                call.respond(HttpStatusCode.BadRequest, "{\"error\":\"Operator has already been selected.\"}")
                return@get
            }

            // Adiciona o operador escolhido à lista de operadores selecionados
            if (operatorChooseName != null) {
                selectedOperators.add(operatorChooseName)
            }

            val validator = OperatorValidator()
            val returnValue = validator.validateOperatorComparison(operatorChooseName, operatorRandomName)

            if (returnValue.contains("error")) {
                call.respond(HttpStatusCode.BadRequest, returnValue)
            } else {
                call.respond(HttpStatusCode.OK, returnValue)
            }
        }
    }
}
