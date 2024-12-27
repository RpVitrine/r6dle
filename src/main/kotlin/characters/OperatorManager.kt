package com.example

//import com.google.gson.Gson
//import java.io.File
//import kotlin.random.Random
//
//data class Operator(
//    val name: String,
//    val gender: String,
//    val role: String,
//    val side: String,
//    val country: String,
//    val org: String,
//    val squad: String,
//    val releaseYear: Int
//)
//
//class OperatorManager(private val jsonPath: String) {
//    private val operators: List<Operator>
//    private var selectedOperator: Operator? = null
//
//    init {
//        // Lê o arquivo JSON ao inicializar a classe
//        val jsonContent = File(jsonPath).readText()
//        operators = Gson().fromJson(jsonContent, Array<Operator>::class.java).toList()
//    }
//
//    fun selectRandomOperator(): Operator {
//        selectedOperator = operators[Random.nextInt(operators.size)]
//        return selectedOperator!!
//    }
//
//    fun checkOperatorName(name: String): Boolean {
//        return selectedOperator?.name?.equals(name, ignoreCase = true) == true
//    }
//
//    fun getSelectedOperator(): Operator? = selectedOperator
//}
