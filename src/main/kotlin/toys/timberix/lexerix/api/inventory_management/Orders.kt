@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.inventory_management

import kotlinx.datetime.Clock
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import toys.timberix.lexerix.api.utils.DatedIntIdTable

object Orders : DatedIntIdTable("FK_Auftrag", "SheetNr") {
    val auftragsNr = varchar("AuftragsNr", 20)
    val auftragsKennung = integer("AuftragsKennung").default(1) // ?

    // customer
    val kundenNr = reference("KundenNr", Customers.kundenNr)
    val kundenMatchcode = varchar("KundenMatchcode", 35)

    // Konditionen
    /** 1, 2 or 3 */
    val preisGrp = integer("Konditionen_PreisgrpNr").default(1)
    val rabatt = float("Konditionen_Rabatt").default(0f)
    val rabattProz = float("Konditionen_Rabatt_Proz").default(0f)

    /** 978 = € */
    val currency = integer("Konditionen_Waehrung")

    /** 4 = online */
    val zahlungsArt = integer("Konditionen_Zahlungsart").default(4)

    /** "Lieferung per Postversand" oder "Lieferung frei Haus" */
    val lieferArt = varchar("Konditionen_Lieferart", 200).default("Lieferung per Postversand")

    // Anschrift
    val anschriftAnrede = varchar("Anschrift_Anrede", 20)
    val anschriftFirma = varchar("Anschrift_Firma", 60)
    val anschriftName = varchar("Anschrift_Name", 50)
    val anschriftVorname = varchar("Anschrift_Vorname", 50)
    val anschriftZusatz = varchar("Anschrift_Zusatz", 60)
    val anschriftStrasse = varchar("Anschrift_Strasse", 35)
    val anschriftHausNr = varchar("Anschrift_HausNr", 20)
    val anschriftOrt = varchar("Anschrift_Ort", 35)
    val anschriftPlz = varchar("Anschrift_Plz", 10)
    val anschriftLand = varchar("Anschrift_Land", 35)
    val anschriftTel1 = varchar("Anschrift_Tel1", 20)
    val anschriftTel2 = varchar("Anschrift_Tel2", 20)
    val anschriftEmail = varchar("Anschrift_Email", 255)

    // timestamps
    val datumErfassung = timestamp("Datum_erfassung").clientDefault { Clock.System.now() }
    val datumZahlung = timestamp("Datum_zahlung")

    // prices
    val nettoHaupt = float("Summen_netto_haupt")
    val bruttoHaupt = float("Summen_brutto_haupt")
    val nettoNeben = float("Summen_netto_neben").default(0f)
    val bruttoNeben = float("Summen_brutto_neben").default(0f)

    /** only USt */
    val totalTax = float("Summen_ust_gesamt")

    /** total gross price with tax */
    val totalGrossPrice = float("Summen_gesamt")

    // todo Abschlag blablabla?
    val abschlagForderung = float("Summen_abschlag_forderung") // the same as "Summen_gesamt"?

    // statuses
    val lagergebucht = integer("bStatus_lagergebucht").default(0)
    val gebucht = integer("bStatus_gebucht").default(0)
    val uebernommen = integer("bStatus_uebernommen").default(0)

    // what is "Status_gedruckt" and "Status_drucken"?!?
    val gedruckt = integer("bStatus_gedruckt").default(0)
    val exportiert = integer("bStatus_exportiert").default(0)

    /** was the order delivered? */
    val delivered = integer("bStatus_geliefert").default(0)

    /** was the order paid? */
    val paid = integer("bStatus_bezahlt").default(0)
    val weitergefuehrt = integer("bStatus_weitergefuehrt").default(0)

    /** expected delivery *date* (time does not matter) */
    val deliveryDate = timestamp("tsLieferTermin").clientDefault { Clock.System.now() }

    // lHerkunft ?

    val gewinnNetto = float("dftSumme_GewinnNetto_NAR").default(0f) // ?

    val weight = float("dftSummen_Gewicht").default(0f)

    val userDefined1 = varchar("szUserdefined1", 50)

    fun selectWithProducts() = ((this innerJoin OrderContents) innerJoin Products)
        .selectAll()
        .groupBy {
            it[id]
        }

    /** Inserts an order with `id` and `AuftragsNr` that are not yet used. */
    fun insertUnique(block: Orders.(InsertStatement<EntityID<Int>>) -> Unit): EntityID<Int> {
        // Fetch the highest id
        val orders = selectAll()
        val highestId = orders.maxOf { maxOf(it[id].value, it[auftragsNr].toIntOrNull() ?: 0) }

        // Insert new customer
        return insertAndGetId {
            it[id] = highestId + 1
            it[auftragsNr] = (highestId + 1).toString()
            block(it)
        }
    }

    /**
     * Inserts an order with calculated prices and the given products as [OrderContents].
     */
    fun insertWithProducts(
        vararg products: OrderContentData,
        block: Orders.(InsertStatement<EntityID<Int>>) -> Unit
    ): EntityID<Int> {
        val orderId = insertUnique {
            // Calculate price
            val netPrice = products.sumOf { data ->
                data.netPrice()
            }
            val taxPortion = 0.19.toBigDecimal() // todo check tax
            val tax = netPrice * taxPortion
            val grossPrice = netPrice + tax

            it[nettoHaupt] = netPrice.toFloat()
            it[bruttoHaupt] = grossPrice.toFloat()
            it[totalTax] = tax.toFloat()
            it[totalGrossPrice] = grossPrice.toFloat()
            it[abschlagForderung] = grossPrice.toFloat()

            block(it)
        }
        OrderContents.insertFor(orderId.value.toString(), *products)
        return orderId
    }
}

context(Orders)
@Suppress("USELESS_ELVIS", "RemoveRedundantQualifierName")
fun InsertStatement<EntityID<Int>>.applyCustomerData(customer: ResultRow) {
    val mapping = mapOf(
        Orders.anschriftAnrede to Customers.anschriftAnrede,
        Orders.anschriftFirma to Customers.anschriftFirma,
        Orders.anschriftName to Customers.anschriftName,
        Orders.anschriftVorname to Customers.anschriftVorname,
        Orders.anschriftZusatz to Customers.anschriftZusatz,
        Orders.anschriftStrasse to Customers.anschriftStrasse,
        Orders.anschriftHausNr to Customers.anschriftHausNr,
        Orders.anschriftOrt to Customers.anschriftOrt,
        Orders.anschriftPlz to Customers.anschriftPlz,
        Orders.anschriftLand to Customers.anschriftLand,
        Orders.anschriftTel1 to Customers.anschriftTel1,
        Orders.anschriftTel2 to Customers.anschriftTel2,
        Orders.anschriftEmail to Customers.anschriftEmail
    )

    mapping.forEach { (orderColumn, customerColumn) ->
        this[orderColumn] = customer[customerColumn] ?: ""
    }
}