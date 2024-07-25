import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import toys.timberix.lexerix.api.CURRENCY_EUR
import toys.timberix.lexerix.api.inventory_management.InventoryManagement
import toys.timberix.lexerix.api.inventory_management.InventoryManagement.Customers
import toys.timberix.lexerix.api.inventory_management.InventoryManagement.Orders
import toys.timberix.lexerix.api.inventory_management.InventoryManagement.applyCustomerData
import toys.timberix.lexerix.api.roundToCurrency
import kotlin.time.Duration.Companion.hours

fun main() {
    exampleSetup()

    insertOrder()
}

private fun insertOrder() {
    // get customer
    val customer = transaction {
        Customers.selectAll().first()
    }
    println("Using customer ${customer[Customers.anschriftName]} (${customer[Customers.kundenNr]})")

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
            val nettoPrice = 0f
            val tax = nettoPrice * 0.19f
            val bruttoPrice = nettoPrice + tax
            it[nettoHaupt] = nettoPrice
            it[bruttoHaupt] = bruttoPrice
            it[totalTax] = tax
            it[totalGrossPrice] = bruttoPrice
            it[abschlagForderung] = bruttoPrice

            it[deliveryDate] = Clock.System.now().plus(24.hours)
        }
    }

    println("Inserted empty order with id $id")

    // Insert order with products: 2x product1 and 1x product2
    val (product1, product2) = transaction {
        InventoryManagement.Products.withPrices().andWhere {
            InventoryManagement.Products.webShop eq true
        }.take(2)
    }

    val orderId = transaction {
        Orders.insertUnique {
            it[kundenNr] = customer[Customers.kundenNr]
            it[kundenMatchcode] = customer[Customers.matchcode]
            it[currency] = CURRENCY_EUR
            it.applyCustomerData(customer)

            val netPrice = product1[InventoryManagement.PriceMatrix.vkPreisNetto] * 2 + product2[InventoryManagement.PriceMatrix.vkPreisNetto]
            val taxPortion = 0.19f
            val tax = (netPrice * taxPortion).roundToCurrency()
            val grossPrice = (netPrice + tax).roundToCurrency()
            println("Calculated prices: net $netPrice, gross $grossPrice, tax $tax")
            it[nettoHaupt] = netPrice
            it[bruttoHaupt] = grossPrice
            it[totalTax] = tax
            it[totalGrossPrice] = grossPrice
            it[abschlagForderung] = grossPrice

            it[deliveryDate] = Clock.System.now().plus(48.hours)
        }
    }

    // contents
    transaction {
        InventoryManagement.OrderContents.insertFor(
            orderId.value.toString(),
            InventoryManagement.OrderContentData(
                product1,
                2,
                "I want two of '${product1[InventoryManagement.Products.bezeichnung]}'"
            ),
            InventoryManagement.OrderContentData(
                product2,
                1,
                "And one of '${product2[InventoryManagement.Products.bezeichnung]}'"
            )
        )
    }

    println("Inserted order (2x ${product1[InventoryManagement.Products.bezeichnung]}, " +
            "1x ${product2[InventoryManagement.Products.bezeichnung]}) with id $orderId")
}