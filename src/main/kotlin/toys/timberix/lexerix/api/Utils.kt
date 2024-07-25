package toys.timberix.lexerix.api

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

fun String.asCurrency() = BigDecimal(this, MathContext(2, RoundingMode.HALF_UP))
fun Float.asCurrency(): BigDecimal = BigDecimal.valueOf(toDouble()).setScale(2, RoundingMode.HALF_UP)
