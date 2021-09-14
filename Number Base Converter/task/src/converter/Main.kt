package converter

import java.math.BigDecimal
import kotlin.math.pow
import java.math.BigInteger
import java.math.RoundingMode

fun main() {
    var convType = ""

    while (convType != "/exit") {
        print("Enter two numbers in format: {source base} {target base} (To quit type /exit) ")
        convType = readLine()!!.toString()

        if (convType == "/exit") {
            break
        } else {
            val (sourceBase, targetBase) = convType.split(" ", limit = 2).toTypedArray()
            var numberToConv = ""

            while (numberToConv != "/back") {
                print("Enter number in base $sourceBase to convert to base $targetBase (To go back type /back) ")
                numberToConv = readLine()!!.toString()
                if (numberToConv == "/back") {
                    break
                } else {
                    var targetNumber = ""
                    var targetFractional = ""
                    if (numberToConv.contains(".")) {
                        val breakPoint = numberToConv.indexOf(".")
                        val startNumberToConv = numberToConv.substringBefore(".")
                        val endOfNumberToConv = numberToConv.substringAfter(".")

                        val decNumber = sourceToDecimal(sourceBase.toInt(), startNumberToConv.toList())
                        val decFractional = sourceToFractionalDecimal(sourceBase.toInt(), endOfNumberToConv.toList())

                        targetNumber = decimalToTarget(targetBase.toInt(), decNumber)
                        targetFractional = decimalFractionalToTarget(targetBase.toInt(), decFractional)
                    } else {
                        val decNumber = sourceToDecimal(sourceBase.toInt(), numberToConv.toList())
                        targetNumber = decimalToTarget(targetBase.toInt(), decNumber)
                    }
                    println(
                        "Conversion result: $targetNumber"
                        + if (targetFractional != "") {
                            ".$targetFractional"
                        } else {
                            ""
                        }
                    )
                }
            }
        }
    }
}

fun sourceToDecimal(sourceBase: Int, sourceNumber: List<Char>): BigInteger {
    var sum = BigInteger.ZERO
    var places = sourceNumber.size - 1
    val sourceBI = sourceBase.toBigInteger()

    for (digit in sourceNumber) {
        if (digit.isDigit()) {
            sum += digit.digitToInt().toBigInteger() * sourceBI.pow(places)
        } else {
            if (digit.lowercaseChar() in 'a'..'z') {
                sum += ((digit.code - 97) + 10).toBigInteger() * sourceBI.pow(places)
            }
        }
        places--
    }

    return sum
}

fun decimalToTarget(targetBase: Int, decimalSource: BigInteger): String {
    var targetNumber = ""
    var quotient = decimalSource

    // var quotient1 = quotient
    while (quotient > BigInteger.ZERO) {
        val (quotientAfterDaR, remainder) = quotient.divideAndRemainder(targetBase.toBigInteger())
        targetNumber = if (remainder < BigInteger.TEN) {
            remainder.toString() + targetNumber
        } else {
            val digitChar = (remainder.toInt() - 10 + 97).toChar()
            digitChar + targetNumber
        }
        quotient = quotientAfterDaR
    }

    // catch the fall through case of starting with zero
    if (decimalSource == BigInteger.ZERO) {
        targetNumber = "0"
    }
    return targetNumber
}

fun sourceToFractionalDecimal(sourceBase: Int, sourceFractional: List<Char>): BigDecimal {
    var sum = BigDecimal.ZERO.setScale(5)
    var places = 1  // WAS:  sourceFractional.size - 1, moving to decrementer...
    val sourceBI = sourceBase.toBigDecimal().setScale(5)

    for (digit in sourceFractional) {
        if (digit.isDigit()) {
            sum += digit.digitToInt().toBigDecimal().setScale(5) / sourceBI.pow(places)
        } else {
            if (digit.lowercaseChar() in 'a'..'z') {
                sum += ((digit.code - 97) + 10).toBigDecimal().setScale(5) / sourceBI.pow(places)
            }
        }
        places++
    }

    return sum
}

fun decimalFractionalToTarget(sourceBase: Int, fractionalSource: BigDecimal): String {
    var targetNumber = ""
    val bigdecBase = sourceBase.toBigDecimal().setScale(5)
    var maxPasses = 5  // hate using a counter for this, but it keeps the loop from going infinite/a long time.

    var result = fractionalSource * bigdecBase
    result = result.setScale(5)
    while (result != BigDecimal.ZERO.setScale(5) && maxPasses > 0) {
        // first handle integer part of result
        val targetInt = result.setScale(0, RoundingMode.DOWN).toInt()
        targetNumber = if (targetInt < 10) {
            targetNumber + targetInt.toString()
        } else {
            val digitChar = (targetInt - 10 + 97).toChar()
            targetNumber + digitChar
        }

        // second remove integer part from fractional part
        result -= targetInt.toBigDecimal()
        result *= bigdecBase
        result = result.setScale(5)
        maxPasses--
    }

    return targetNumber.padEnd(length = 5, padChar = '0')
}

