import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import toys.timberix.lexerix.api.CURRENCY_EUR
import toys.timberix.lexerix.api.asCurrency
import toys.timberix.lexerix.api.inventory_management.*
import kotlin.time.Duration.Companion.hours

private fun main() {
    exampleSetup()

    // get customer
    val customer = transaction {
        Customers.selectAll().first()
    }
    println("Using customer ${customer[Customers.anschriftName]} (${customer[Customers.kundenNr]})")

    insertEmptyOrder(customer)
    insertOrderWithProducts(customer)
}

private fun insertEmptyOrder(customer: ResultRow) {
    val id = transaction {
        Orders.insertUnique {
            // customer
            it[kundenNr] = customer[Customers.kundenNr]
            it[kundenMatchcode] = customer[Customers.matchcode]

            // Konditionen
            it[currency] = CURRENCY_EUR

            // Anschrift
            // when inserting an order for a specific customer, we can use the customer's data:
            it.applyCustomerData(customer)
            // alternatively, we may want to set the data manually:
//            it[anschriftAnrede] = "..."
//            it[anschriftFirma] = "..."
//            it[anschriftName] = "..."
//            it[anschriftVorname] = "..."
//            it[anschriftZusatz] = "..."
//            it[anschriftStrasse] = "..."
//            it[anschriftHausNr] = "..."
//            it[anschriftOrt] = "..."
//            it[anschriftPlz] = "..."
//            it[anschriftLand] = "..."
//            it[anschriftTel1] = "..."
//            it[anschriftTel2] = "..."
//            it[anschriftEmail] = "..."

            //  prices
            val netPrice = 0f.asCurrency()
            val tax = netPrice * 0.19.toBigDecimal()
            val grossPrice = netPrice + tax
            it[nettoHaupt] = netPrice.toFloat()
            it[bruttoHaupt] = grossPrice.toFloat()
            it[totalTax] = tax.toFloat()
            it[totalGrossPrice] = grossPrice.toFloat()
            it[abschlagForderung] = grossPrice.toFloat()

            it[deliveryDate] = Clock.System.now().plus(24.hours)
        }
    }

    println("Inserted empty order with id $id")
}

private fun insertOrderWithProducts(customer: ResultRow) {
    // Insert order with products: 2x product1 and 1x product2
    val (product1, product2) = transaction {
        Products.withPrices().andWhere {
            Products.webShop eq true
        }.take(2)
    }

    val orderId = transaction {
        Orders.insertWithProducts(
            OrderContentData(
                product1,
                2,
                "I want two of '${product1[Products.bezeichnung]}'"
            ),
            OrderContentData(
                product2,
                1,
                "And one of '${product2[Products.bezeichnung]}'"
            )
        ) {
            it[kundenNr] = customer[Customers.kundenNr]
            it[kundenMatchcode] = customer[Customers.matchcode]
            it[currency] = CURRENCY_EUR
            it.applyCustomerData(customer)

            it[deliveryDate] = Clock.System.now().plus(48.hours)
        }
    }

    println("Inserted order (2x ${product1[Products.bezeichnung]}, " +
            "1x ${product2[Products.bezeichnung]}) with id $orderId")
}