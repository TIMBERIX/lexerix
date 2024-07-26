package toys.timberix.lexerix.api.utils

import java.math.BigDecimal
import java.math.RoundingMode

fun Float.asCurrency(): BigDecimal = BigDecimal.valueOf(toDouble()).setScale(2, RoundingMode.HALF_UP)
fun Float.asWeight(): BigDecimal = BigDecimal.valueOf(toDouble()).setScale(3, RoundingMode.HALF_UP)
