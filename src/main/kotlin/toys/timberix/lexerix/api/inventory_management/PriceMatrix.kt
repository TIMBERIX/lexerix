@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.inventory_management

import toys.timberix.lexerix.api.utils.DatedTable

object PriceMatrix : DatedTable("FK_Preismatrix") {
        val artikelNr = reference("ArtikelNr", Products.artikelNr)
        val mengeNr = integer("MengeNr")
        val preisGrpNr = integer("PreisGrpNr")

        /** Net or gross (!) selling price in € */
        val price = float("Vk_preis_eur")
    }