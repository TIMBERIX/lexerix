@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.inventory_management

import toys.timberix.lexerix.api.utils.DatedTable

object PriceMatrix : DatedTable("FK_Preismatrix") {
        val productNr = reference("ArtikelNr", Products.productNr)
        val mengeNr = integer("MengeNr")
        val preisGrpNr = integer("PreisGrpNr")

        /**
         * Selling price in €.
         *
         * Whether this is net or gross price depends on the user settings in Lexware.
         */
        val sellingPrice = float("Vk_preis_eur")
    }