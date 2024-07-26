@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.inventory_management

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import toys.timberix.lexerix.api.utils.DatedTable
import toys.timberix.lexerix.api.utils.asCurrency
import toys.timberix.lexerix.api.utils.asWeight
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object OrderContents : DatedTable("FK_AuftragPos") {
        val lNr = integer("lNr")
        val auftragsNr = reference("AuftragsNr", Orders.auftragsNr)
        val auftragsKennung = integer("AuftragsKennung").default(1) // ?

        /** index of content in order, e.g. order 1 could have contents 1,2,3 and order 2 could have contents 1,2 */
        val lfdNumber = integer("LfdNr")
        val szPosNr = integer("szPosNr") // = lfdNumber (?)
        val posNumber = integer("PosNr") // 1 bigger than lfdNumber (?)

        /** additional text per content, e.g. "I want this (the apples) to be green" */
        val note = varchar("PosText", 5000).default("")
        val productNr = reference("ArtikelNr", Products.artikelNr)

        // "ErloesKonto" doesn't seem to matter
        val warengrpNr = integer("WarengrpNr").default(1)

        // product data (why is this duplicated?)
        val productName = varchar("Artikel_Bezeichnung", 100)
        val productMatchcode = varchar("Artikel_Matchcode", 35)
        val productUnit = varchar("Artikel_Einheit", 20).default("Stück")
        val productWeight = float("Artikel_kg_Einheit")

        val count = integer("Artikel_Menge")
        val priceFactor = integer("Artikel_Preisfaktor") // = count
        val netPricePerProduct = float("Summen_preis") // duplicate of netProductCost?
        val totalNetPrice = float("Summen_netto")
        val totalGrossPrice = float("Summen_brutto")
        val taxProz = float("Summen_ust_proz").default(19f)

        /** calculated tax for this content */
        val totalTax = float("Summen_ust_gesamt")

        val totalTaxAfterAufrab = float("Summen_ust_nach_Aufrab") // = totalTax
        val totalNetPriceAfterAufrab = float("Summen_netto_nach_Aufrab") // = totalPriceNetto
        val totalGrossPriceAfterAufrab = float("Summen_brutto_nach_Aufrab") // = totalPriceBrutto
        val mitAuftragsRabatt = integer("bMitAuftragsrabatt").default(1)

        val productId = integer("lArtikelID") // = SheetNr of product

        val rabattWarnung = integer("fRabattWarnung").default(1)
        val productShortDesc = varchar("szArtikel_Kurzbezeichnung", 100) // often eq. productName

        // cost of product for company without profit -- not used?
        val productCost = float("dftArtikel_Selbstkosten")
        val netProductCost = float("dftArtikelpreisNetto")
        val grossProductCost = float("dftArtikelpreisBrutto")

        val gewinnNetto = float("dftSumme_GewinnNetto_NAR") // doesn't seem to be used

        val weight = float("dftSummen_Gewicht")

        // tsLieferRueckstand ?

        /** Inserts an order content with `id` that is not yet used. */
        fun insertUnique(block: OrderContents.(InsertStatement<Number>) -> Unit): InsertStatement<Number> {
            // Fetch the highest id
            val orders = selectAll()
            val highestId = orders.maxOf { it[lNr] }

            // Insert new customer
            return insert {
                // for some reason, the id is not auto-incremented
                it[lNr] = highestId + 1
                block(it)
            }
        }

        /**
         * @param product the product inner joined with [PriceMatrix]!! ([Products.withPrices])
         * @param index index of the content in the order, starting at 0
         */
        fun insertFor(
            orderNr: String,
            index: Int,
            product: ResultRow,
            count: Int,
            note: String = ""
        ) = insertUnique {
            it[auftragsNr] = orderNr

            it[lfdNumber] = index + 1
            it[szPosNr] = index + 1
            it[posNumber] = index + 2

            it[this.note] = note
            it[productNr] = product[Products.artikelNr]
            it[productName] = product[Products.bezeichnung]
            it[productMatchcode] = product[Products.matchcode]
            it[productUnit] = product[Products.unit]
            it[productWeight] = BigDecimal(product[Products.gewicht].toString(), MathContext(3, RoundingMode.HALF_UP)).toFloat()

            it[this.count] = count
            it[priceFactor] = count
            val netPrice = product[PriceMatrix.vkPreisNetto].asCurrency()
            val taxPortion = BigDecimal("0.19") // todo check tax
            val tax = netPrice * taxPortion
            val grossPrice = netPrice + tax
            val totalNetPrice = netPrice * BigDecimal(count)
            val totalTax = totalNetPrice * taxPortion
            val totalGrossPrice = totalNetPrice + totalTax
            it[netPricePerProduct] = netPrice.toFloat()
            it[this.totalNetPrice] = totalNetPrice.toFloat()
            it[this.totalGrossPrice] = totalGrossPrice.toFloat()
            it[taxProz] = taxPortion.toFloat()
            it[this.totalTax] = totalTax.toFloat()

            it[totalTaxAfterAufrab] = totalTax.toFloat()
            it[totalNetPriceAfterAufrab] = totalNetPrice.toFloat()
            it[totalGrossPriceAfterAufrab] = totalGrossPrice.toFloat()

            it[productId] = product[Products.id].value
            it[productShortDesc] = product[Products.bezeichnung]

            it[netProductCost] = netPrice.toFloat()
            it[grossProductCost] = grossPrice.toFloat()

            it[weight] = (product[Products.gewicht].asWeight() * count.toBigDecimal()).toFloat()
        }

        fun insertFor(orderNr: String, vararg products: OrderContentData) {
            products.forEachIndexed { index, data ->
                insertFor(orderNr, index, data.product, data.count, data.note)
            }
        }
    }

    data class OrderContentData(
        val product: ResultRow,
        val count: Int,
        val note: String = ""
    ) {
        fun netPrice() = product[PriceMatrix.vkPreisNetto].asCurrency() * BigDecimal(count)
    }