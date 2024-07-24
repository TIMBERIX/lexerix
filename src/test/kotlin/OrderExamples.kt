import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import toys.timberix.lexerix.api.CURRENCY_EUR
import toys.timberix.lexerix.api.inventory_management.InventoryManagement.Customers
import toys.timberix.lexerix.api.inventory_management.InventoryManagement.Orders
import toys.timberix.lexerix.api.inventory_management.InventoryManagement.applyCustomerData
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
            it[totalPrice] = bruttoPrice
            it[abschlagForderung] = bruttoPrice

            it[deliveryDate] = Clock.System.now().plus(48.hours)
        }
    }
    println("Inserted order with id $id")
}