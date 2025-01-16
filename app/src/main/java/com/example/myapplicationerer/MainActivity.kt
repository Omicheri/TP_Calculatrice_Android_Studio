package com.example.myapplicationerer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

data class CalculationHistoryItem(
    val firstNumber: Double,
    val secondNumber: Double,
    val operator: String,
    val result: Double,
    val timestamp: Long = System.currentTimeMillis()
)

class MainActivity : AppCompatActivity() {
    private lateinit var resultTextView: TextView
    private lateinit var historyTextView: TextView
    private var firstNumber: Double = 0.0
    private var operator: String = ""
    private var isNewOperation: Boolean = true
    private var hasDecimalPoint: Boolean = false
    private val calculationHistory = mutableListOf<CalculationHistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultTextView = findViewById(R.id.resultTextView)
        historyTextView = findViewById(R.id.historyTextView)

        // Configuration des boutons numériques
        val buttonIds = listOf(
            R.id.button0, R.id.button1, R.id.button2, R.id.button3,
            R.id.button4, R.id.button5, R.id.button6, R.id.button7,
            R.id.button8, R.id.button9
        )

        for (buttonId in buttonIds) {
            val button = findViewById<Button>(buttonId)
            button.setOnClickListener { onDigitClick(button.text.toString()) }
        }

        findViewById<Button>(R.id.buttonAdd).setOnClickListener { onOperatorClick("+") }
        findViewById<Button>(R.id.buttonSubtract).setOnClickListener { onOperatorClick("-") }
        findViewById<Button>(R.id.buttonMultiply).setOnClickListener { onOperatorClick("*") }
        findViewById<Button>(R.id.buttonDivide).setOnClickListener { onOperatorClick("/") }
        findViewById<Button>(R.id.buttonEquals).setOnClickListener { onEqualsClick() }
        findViewById<Button>(R.id.buttonClear).setOnClickListener { onClearClick() }
        findViewById<Button>(R.id.buttonHistory).setOnClickListener { showHistoryDialog() }
    }

    private fun onDigitClick(digit: String) {
        if (isNewOperation) {
            resultTextView.text = digit
            isNewOperation = false
        } else {
            resultTextView.text = "${resultTextView.text}$digit"
        }
    }

    private fun onOperatorClick(op: String) {
        try {
            firstNumber = resultTextView.text.toString().toDouble()
            operator = op
            isNewOperation = true
            hasDecimalPoint = false
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Format de nombre invalide", Toast.LENGTH_SHORT).show()
            onClearClick()
        }
    }

    private fun onEqualsClick() {
        if (operator.isNotEmpty()) {
            try {
                val secondNumber = resultTextView.text.toString().toDouble()
                val result = calculate(firstNumber, secondNumber, operator)

                // Formater le résultat pour éviter les décimales inutiles
                val formattedResult = if (result % 1 == 0.0) {
                    result.toInt().toString()
                } else {
                    result.toString()
                }

                // Ajouter l'opération à l'historique
                val historyItem = CalculationHistoryItem(
                    firstNumber = firstNumber,
                    secondNumber = secondNumber,
                    operator = operator,
                    result = result
                )
                calculationHistory.add(historyItem)
                updateHistoryView()

                resultTextView.text = formattedResult
                operator = ""
                isNewOperation = true
                hasDecimalPoint = false
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Format de nombre invalide", Toast.LENGTH_SHORT).show()
                onClearClick()
            } catch (e: ArithmeticException) {
                Toast.makeText(this, "Erreur de calcul", Toast.LENGTH_SHORT).show()
                onClearClick()
            }
        }
    }

    private fun calculate(first: Double, second: Double, op: String): Double {
        return when (op) {
            "+" -> first + second
            "-" -> first - second
            "*" -> first * second
            "/" -> {
                if (second == 0.0) throw ArithmeticException("Division par zéro")
                first / second
            }
            else -> throw IllegalArgumentException("Opérateur invalide")
        }
    }

    private fun onClearClick() {
        resultTextView.text = "0"
        firstNumber = 0.0
        operator = ""
        isNewOperation = true
        hasDecimalPoint = false
    }

    private fun updateHistoryView() {
        val historyText = calculationHistory.takeLast(5).joinToString("\n") { item ->
            val operatorSymbol = when (item.operator) {
                "*" -> "×"
                "/" -> "÷"
                else -> item.operator
            }
            "${item.firstNumber} $operatorSymbol ${item.secondNumber} = ${item.result}"
        }
        historyTextView.text = historyText
    }

    private fun showHistoryDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Historique des calculs")
            .setMessage(calculationHistory.joinToString("\n") { item ->
                val operatorSymbol = when (item.operator) {
                    "*" -> "×"
                    "/" -> "÷"
                    else -> item.operator
                }
                "${item.firstNumber} $operatorSymbol ${item.secondNumber} = ${item.result}"
            })
            .setPositiveButton("Fermer", null)
            .setNeutralButton("Effacer l'historique") { _, _ ->
                calculationHistory.clear()
                updateHistoryView()
            }
            .create()
        dialog.show()
    }
}