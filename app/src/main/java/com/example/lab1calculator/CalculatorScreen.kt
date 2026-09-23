package com.example.lab1calculator

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs

private const val MAX_INPUT_LENGTH = 16

@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier
) {
    var display by rememberSaveable { mutableStateOf("0") }
    var accumulator by rememberSaveable { mutableDoubleStateOf(0.0) }
    var operation by rememberSaveable { mutableStateOf("") }
    var freshInput by rememberSaveable { mutableStateOf(true) }
    var error by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val errorText = stringResource(R.string.calculation_error)

    fun clear() {
        display = "0"
        accumulator = 0.0
        operation = ""
        freshInput = true
        error = false
    }

    fun inputDigit(digit: String) {
        if (error) {
            clear()
        }

        if (!freshInput && display.length >= MAX_INPUT_LENGTH) {
            return
        }

        if (freshInput || display == "0") {
            display = digit
            freshInput = false
        } else {
            display += digit
        }
    }

    fun inputDecimalPoint() {
        if (error) {
            clear()
        }

        if (freshInput) {
            display = "0."
            freshInput = false
        } else if (!display.contains(".") && display.length < MAX_INPUT_LENGTH) {
            display += "."
        }
    }

    fun showError() {
        error = true
        operation = ""
        freshInput = true
    }

    fun calculate(first: Double, second: Double, currentOperation: String): Double {
        return when (currentOperation) {
            "+" -> first + second
            "−" -> first - second
            "×" -> first * second
            "÷" -> first / second
            else -> second
        }
    }

    fun formatResult(value: Double): String {
        if (value == 0.0) {
            return "0"
        }

        if (abs(value) < 0.0000000001) {
            return String.format(Locale.US, "%.8e", value)
        }

        val result = String.format(Locale.US, "%.10f", value)
            .trimEnd('0')
            .trimEnd('.')

        return if (result.length <= MAX_INPUT_LENGTH) {
            result
        } else {
            String.format(Locale.US, "%.8e", value)
        }
    }

    fun inputOperation(newOperation: String) {
        if (error) {
            return
        }

        if (operation.isNotEmpty() && !freshInput) {
            val secondOperand = display.toDouble()
            if (operation == "÷" && secondOperand == 0.0) {
                showError()
                return
            }

            val result = calculate(accumulator, secondOperand, operation)
            display = formatResult(result)
            accumulator = result
        } else {
            accumulator = display.toDouble()
        }

        operation = newOperation
        freshInput = true
    }

    fun calculateResult() {
        if (error || operation.isEmpty() || freshInput) {
            return
        }

        val secondOperand = display.toDouble()
        if (operation == "÷" && secondOperand == 0.0) {
            showError()
            return
        }

        val result = calculate(accumulator, secondOperand, operation)
        display = formatResult(result)
        accumulator = result
        operation = ""
        freshInput = true
    }

    fun pressKey(key: String) {
        when (key) {
            "C" -> clear()
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> inputDigit(key)
            "." -> inputDecimalPoint()
            "+", "−", "×", "÷" -> inputOperation(key)
            "=" -> calculateResult()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.8f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = if (error) errorText else display,
                    fontSize = 32.sp,
                    maxLines = 1
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(ClipboardManager::class.java)
                        clipboard?.setPrimaryClip(ClipData.newPlainText("", display))
                    },
                    enabled = !error
                ) {
                    Text(stringResource(R.string.copy_result))
                }
            }
        }

        CalculatorKeyboard(
            onKey = { key -> pressKey(key) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(3f)
        )
    }
}

@Composable
private fun CalculatorKeyboard(
    onKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            CalculatorButton(
                text = stringResource(R.string.key_clear),
                onClick = { onKey("C") },
                modifier = Modifier.weight(3f)
            )
            CalculatorButton(
                text = stringResource(R.string.key_divide),
                onClick = { onKey("÷") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            CalculatorButton(stringResource(R.string.key_7), { onKey("7") }, Modifier.weight(1f))
            CalculatorButton(stringResource(R.string.key_8), { onKey("8") }, Modifier.weight(1f))
            CalculatorButton(stringResource(R.string.key_9), { onKey("9") }, Modifier.weight(1f))
            CalculatorButton(
                stringResource(R.string.key_multiply),
                { onKey("×") },
                Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            CalculatorButton(stringResource(R.string.key_4), { onKey("4") }, Modifier.weight(1f))
            CalculatorButton(stringResource(R.string.key_5), { onKey("5") }, Modifier.weight(1f))
            CalculatorButton(stringResource(R.string.key_6), { onKey("6") }, Modifier.weight(1f))
            CalculatorButton(
                stringResource(R.string.key_subtract),
                { onKey("−") },
                Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            CalculatorButton(stringResource(R.string.key_1), { onKey("1") }, Modifier.weight(1f))
            CalculatorButton(stringResource(R.string.key_2), { onKey("2") }, Modifier.weight(1f))
            CalculatorButton(stringResource(R.string.key_3), { onKey("3") }, Modifier.weight(1f))
            CalculatorButton(stringResource(R.string.key_add), { onKey("+") }, Modifier.weight(1f))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            CalculatorButton(
                text = stringResource(R.string.key_0),
                onClick = { onKey("0") },
                modifier = Modifier.weight(2f)
            )
            CalculatorButton(
                text = stringResource(R.string.key_decimal),
                onClick = { onKey(".") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = stringResource(R.string.key_equals),
                onClick = { onKey("=") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp)
    ) {
        Text(
            text = text,
            fontSize = 20.sp
        )
    }
}
