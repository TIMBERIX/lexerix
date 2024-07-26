@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.inventory_management

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import toys.timberix.lexerix.api.utils.DatedIntIdTable

object Customers : DatedIntIdTable("FK_Kunde", "SheetNr") {
        /**
         * Unique customer number. Must be unique.
         */
        val kundenNr = varchar("KundenNr", 20)

        /**
         * Short name/identifier for the customer. Should be unique.
         */
        val matchcode = varchar("Matchcode", 255)

        val anschriftFirma = varchar("Anschrift_Firma", 255).default("")
        val anschriftAnrede = varchar("Anschrift_Anrede", 255).default("")
        val anschriftName = varchar("Anschrift_Name", 255).default("")
        val anschriftVorname = varchar("Anschrift_Vorname", 255).default("")
        val anschriftZusatz = varchar("Anschrift_Zusatz", 255).default("")
        val anschriftStrasse = varchar("Anschrift_Strasse", 255).default("")
        val anschriftHausNr = varchar("Anschrift_HausNr", 255).default("")
        val anschriftOrt = varchar("Anschrift_Ort", 255).default("")
        val anschriftPlz = varchar("Anschrift_Plz", 255).default("")
        val anschriftLand = varchar("Anschrift_Land", 255).default("")
        val anschriftEmail = varchar("Anschrift_Email", 255).default("")
        val anschriftTel1 = varchar("Anschrift_Tel1", 20).default("")
        val anschriftTel2 = varchar("Anschrift_Tel2", 20).default("")
        val anschriftWeb = varchar("Anschrift_Web", 255).default("")
        val bemerkung = varchar("Bemerkung", 255).default("Customer created by LEXERIX")

        /** Inserts a customer with `id` and `KundenNr` that are not yet used. */
        fun insertUnique(block: Customers.(InsertStatement<EntityID<Int>>) -> Unit): EntityID<Int> {
            // Fetch the highest id
            val customers = selectAll()
            val highestId = customers.maxOf { maxOf(it[id].value, it[kundenNr].toIntOrNull() ?: 0) }

            // Insert new customer
            return insertAndGetId {
                it[id] = highestId + 1
                it[kundenNr] = (highestId + 1).toString()
                block(it)
            }
        }
    }