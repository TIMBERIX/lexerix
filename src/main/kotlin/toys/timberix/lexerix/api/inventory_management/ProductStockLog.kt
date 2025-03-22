@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.inventory_management

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.InsertStatement

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

    /** Inserts a product stock log entry with an `id` that is not yet used. */
    fun insertUnique(block: ProductStockLog.(InsertStatement<Number>) -> Unit): Int {
        // Fetch the highest id
        val logs = selectAll()
        val highestId = logs.maxOfOrNull { it[id] } ?: 0

        // Insert new log entry
        val newId = highestId + 1
        insert {
            it[id] = newId
            block(it)
        }

        return newId
    }
}
