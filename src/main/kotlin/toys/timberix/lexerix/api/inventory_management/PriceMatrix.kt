@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.inventory_management

import toys.timberix.lexerix.api.utils.DatedTable

object PriceMatrix : DatedTable("FK_Preismatrix") {
        val artikelNr = reference("ArtikelNr", Products.artikelNr)
        val mengeNr = integer("MengeNr")
        val preisGrpNr = integer("PreisGrpNr")

        /** Net (!) selling price in € */
        val vkPreisNetto = float("Vk_preis_eur")
    }