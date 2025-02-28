@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.inventory_management

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import toys.timberix.lexerix.api.utils.DatedIntIdTable

/**
 * Represents a product/article in the database. It has the following properties:
 *
 * - [id]: unique identifier (integer!) only used internally by Lexware in the database
 * - [productNr]: unique identifier for the product (string!), must be unique, but can be changed by the user
 * - [matchcode]: short "identifier" for the product (string), used as name
 * - [bezeichnung]: another name (string), sometimes used as a short description of the product
 * - [beschreibung]: a longer description of the product (string)
 *
 * Some tables reference the product by its [id], others by its [productNr] (I don't know why).
 *
 * The product may be a product that is actually sold in the webshop ([webShop] = true) or a product that is
 * a component of another product.
 *
 * To fetch the components of a product, see [ProductComponents].
 */
object Products : DatedIntIdTable("FK_Artikel", "SheetNr") {
    val productNr = varchar("ArtikelNr", 255)
    val warengrpNr = integer("WarengrpNr").default(1)
    val matchcode = varchar("Matchcode", 35)
    val bezeichnung = varchar("Bezeichnung", 255)
    val beschreibung = varchar("Beschreibung", 255).default("Product created by LEXERIX")
    val weight = float("Gewicht")
    val unit = varchar("Einheit", 20).default("Stück")

    // Vk_preis, Menge_bestand, Menge_minbestand are unused in the database

    /** Should this product be visible in the webshop? */
    val webShop = bool("bStatus_WebShop")

    val userDefined1 = varchar("szUserdefined1", 50)

    fun withPrices(priceGroup: Int = 1, quantity: Int = 1) = (this innerJoin PriceMatrix).selectAll().where {
        (PriceMatrix.preisGrpNr eq priceGroup) and (PriceMatrix.mengeNr eq quantity)
    }

    fun withPricesAndStocks(priceGroup: Int = 1, quantity: Int = 1, storeId: Int = 1) =
        ((this innerJoin PriceMatrix) innerJoin ProductStocks).selectAll().where {
            (PriceMatrix.preisGrpNr eq priceGroup) and (PriceMatrix.mengeNr eq quantity) and (ProductStocks.store eq storeId)
        }

    fun withStocks(storeId: Int = 1) = (this innerJoin ProductStocks).selectAll().where {
        (ProductStocks.store eq storeId)
    }
}