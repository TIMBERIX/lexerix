@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.inventory_management

import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import toys.timberix.lexerix.api.utils.DatedTable

object ProductComponents : DatedTable("FK_Stueckliste") {
    val baseProductNr = reference("ArtikelNr", Products.productNr)
    val childProductNr = reference("UnterartikelNr", Products.productNr)
    val amount = integer("Menge")

    fun getForProduct(productNr: String) = (Products.innerJoin(
        ProductComponents,
        onColumn = { Products.productNr },
        otherColumn = { baseProductNr })).selectAll().where { baseProductNr eq productNr }
        .map { it[childProductNr] to it[amount] }
}