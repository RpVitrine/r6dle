package com.example

import Operator
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
import kotlin.reflect.typeOf

val logger: Logger = LoggerFactory.getLogger("ValidateFieldsEndpoint")


val operatorsList = mutableListOf<Operator>()

fun loadOperators() {
    val jsonFile = File("src/main/resources/characters/characters.json")
    if (jsonFile.exists()) {
        val jsonData = jsonFile.readText()
        operatorsList.addAll(Json.decodeFromString(jsonData))
    } else {
        println("JSON file not found at path: src/main/resources/characters/characters.json")
    }
}

fun Application.configureRouting() {
    loadOperators()

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
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
            if (operatorsList.isEmpty()) {
                call.respond(HttpStatusCode.InternalServerError, "{\"error\":\"No operators available.\"}")
                return@get
            }

            val randomOperator = operatorsList.random()
            val jsonResponse = Json.encodeToString(randomOperator)
            call.respond(HttpStatusCode.OK, jsonResponse)
        }

        get("/api/operador-name") {
            val operatorName = call.request.queryParameters["name"]
            if (operatorName.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "{\"error\":\"Operator name is required.\"}")
                return@get
            }

            val selectedOperator = operatorsList.find { it.name.equals(operatorName, ignoreCase = true) }
            if (selectedOperator == null) {
                call.respond(HttpStatusCode.NotFound, "{\"error\":\"Operator not found.\"}")
                return@get
            }

            val jsonResponse = Json.encodeToString(selectedOperator)
            call.respond(HttpStatusCode.OK, jsonResponse)
        }


        get("/api/all-operator-name") {
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
        get("/api/validate-all-fields") {
            val operatorChooseName = call.request.queryParameters["choose"]

            logger.info("Received request with chosen operator: $operatorChooseName")

            if (operatorChooseName.isNullOrBlank()) {
                logger.warn("Missing parameter: choose is null or blank")
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Chosen operator is required.")
                )
                return@get
            }

            val operatorChoose = operatorsList.find { it.name.equals(operatorChooseName, ignoreCase = true) }
            if (operatorChoose == null) {
                logger.warn("Operator not found: choose = $operatorChooseName")
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Operator not found.")
                )
                return@get
            }

            // Passa por cada operador em operatorsList e realiza validações
            val validationResults = operatorsList.map { operatorRandom ->
                val results = mapOf(
                    "name" to if (operatorRandom.name == operatorChoose.name) "green" else "red",
                    "gender" to if (operatorRandom.gender == operatorChoose.gender) "green" else "red",
                    "role" to if (operatorRandom.role == operatorChoose.role) "green" else "red",
                    "side" to if (operatorRandom.side == operatorChoose.side) "green" else "red",
                    "country" to if (operatorRandom.country == operatorChoose.country) "green" else "red",
                    "Org" to if (operatorRandom.Org == operatorChoose.Org) "green" else "red",
                    "Squad" to if (operatorRandom.Squad == operatorChoose.Squad) "green" else "red",
                    "release_year" to if (operatorRandom.release_year == operatorChoose.release_year) "green" else "red"
                )
                mapOf(
                    "randomOperator" to operatorRandom.name,
                    "validation" to results
                )
            }

            logger.info("Validation results for all operators: $validationResults")

            val jsonResponse = mapOf(
                "chosenOperator" to operatorChoose.name,
                "validations" to validationResults
            )

            call.respond(HttpStatusCode.OK, jsonResponse)
        }
    }
}