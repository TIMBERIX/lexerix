@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.inventory_management

import toys.timberix.lexerix.api.utils.DatedTable

object ProductStocks : DatedTable("FK_LagerBestand") {
    val artikelNr = reference("lArtikelId", Products.id)
    val stockAmount = integer("Bestand")
    val store = reference("lLagerId", Stores.id)
    val mengeBestellt = integer("Menge_bestellt").default(0)
}