@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.inventory_management

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import toys.timberix.lexerix.api.utils.DatedIntIdTable

object Products : DatedIntIdTable("FK_Artikel", "SheetNr") {
        val artikelNr = integer("ArtikelNr")
        val matchcode = varchar("Matchcode", 35)
        val bezeichnung = varchar("Bezeichnung", 255)
        val beschreibung = varchar("Beschreibung", 255).default("Product created by LEXERIX")
        val gewicht = float("Gewicht")
        val unit = varchar("Einheit", 20).default("Stück")

        /** no price? --- use [withPrices] */
        val preis = float("Vk_preis")
        val bestand = float("Menge_bestand")
        val minBestand = float("Menge_minbestand")

        /** Should this product be visible in the webshop? */
        val webShop = bool("bStatus_WebShop")

        val userDefined1 = varchar("szUserdefined1", 50)

        fun withPrices(priceGroup: Int = 1, quantity: Int = 1) = (this innerJoin PriceMatrix).selectAll().where {
            (PriceMatrix.preisGrpNr eq priceGroup) and (PriceMatrix.mengeNr eq quantity)
        }
    }