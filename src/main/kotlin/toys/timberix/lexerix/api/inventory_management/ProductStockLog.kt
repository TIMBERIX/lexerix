@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.inventory_management

import org.jetbrains.exposed.sql.Table

object ProductStockLog : Table("FK_LagerJournal") {
    val id = integer("lNr")
    val artikelNr = reference("szArtikelNr", Products.artikelNr)
    val type = integer("lType") // 3 for manual stock change
    val store = reference("lLagerId", Stores.id)
    val description = varchar("szBeschreibung", 100)
    val user = varchar("szUser", 60)
    val difference = integer("dftMenge")
    val stockAfter = integer("dftBestand")
}