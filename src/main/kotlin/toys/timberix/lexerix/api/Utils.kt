package toys.timberix.lexerix.api

import kotlin.math.roundToInt

fun Float.roundToCurrency() = (this * 100).roundToInt() / 100f
