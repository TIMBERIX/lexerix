@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.inventory_management

import org.jetbrains.exposed.sql.Table

object ProductStockLog : Table("FK_LagerJournal") {
    val id = integer("lNr")

    /**
     * The [Products.productNr] of the product that was affected by this stock change.
     */
    val productNr = reference("szArtikelNr", Products.productNr)
    val type = integer("lType") // 3 for manual stock change
    val store = reference("lLagerId", Stores.id)
    val description = varchar("szBeschreibung", 100)
    val user = varchar("szUser", 60)
    val difference = integer("dftMenge")
    val stockAfter = integer("dftBestand")
}