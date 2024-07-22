@file:Suppress("ClassName", "Unused", "MemberVisibilityCanBePrivate")

package toys.timberix.toys.timberix.lexerix.api.inventory_management

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import toys.timberix.lexerix.api.inventory_management.DatedTable

object InventoryManagement {
    object Customers : IntIdTable("FK_Kunde", "SheetNr") {
        /**
         * Unique customer number. Must be unique.
         */
        val kundenNr = integer("KundenNr")

        /**
         * Short name/identifier for the customer. Recommended (but not required) to be unique.
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
        val anschriftWeb = varchar("Anschrift_Web", 255).default("")
        val bemerkung = varchar("Bemerkung", 255).default("Customer created by LEXERIX")

        /** Inserts a customer with `id` and `KundenNr` that are not yet used. */
        fun insertUnique(block: Customers.(InsertStatement<EntityID<Int>>) -> Unit): EntityID<Int> {
            // Fetch the highest id
            val customers = selectAll()
            val highestId = customers.maxOf { maxOf(it[id].value, it[kundenNr]) }

            // Insert new customer
            return insertAndGetId {
                it[id] = highestId + 1
                it[kundenNr] = highestId + 1
                block(it)
            }
        }
    }

    object Companies : IntIdTable("FK_Firma", "lID_Firma") {
        val bBrutto = bool("bBrutto")
    }

    object Products : DatedTable("FK_Artikel", "SheetNr") {
        val artikelNr = integer("ArtikelNr")
        val bezeichnung = varchar("Bezeichnung", 255)
        val beschreibung = varchar("Beschreibung", 255).default("Product created by LEXERIX")
        val gewicht = float("Gewicht")
        /** no price? --- use [withPrices] */
        val preis = float("Vk_preis")
        val bestand = float("Menge_bestand")
        val minBestand = float("Menge_minbestand")
        /** Should this product be visible in the webshop? */
        val webShop = bool("bStatus_WebShop")

        fun withPrices() = innerJoin(PriceMatrix) {
            artikelNr eq PriceMatrix.artikelNr
        }.selectAll().where {
            (PriceMatrix.preisGrpNr eq 1) and (PriceMatrix.mengeNr eq 1)
        }
    }

    object PriceMatrix : Table("FK_Preismatrix") {
        val artikelNr = integer("ArtikelNr")
        val mengeNr = integer("MengeNr")
        val preisGrpNr = integer("PreisGrpNr")
        /** Selling price in € */
        val vkPreisEur = float("Vk_preis_eur")
    }
}