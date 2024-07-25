@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.inventory_management

import org.jetbrains.exposed.sql.Table

object PriceMatrix : Table("FK_Preismatrix") {
        val artikelNr = reference("ArtikelNr", Products.artikelNr)
        val mengeNr = integer("MengeNr")
        val preisGrpNr = integer("PreisGrpNr")

        /** Net (!) selling price in € */
        val vkPreisNetto = float("Vk_preis_eur")
    }