@file:Suppress("ClassName")

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import toys.timberix.lexerix.api.inventory_management.*
import kotlin.time.Duration.Companion.seconds

private fun listAllCustomers() {
    transaction {
        println("Customers:")
        Customers.selectAll().forEach {
            println(" - '${it[Customers.anschriftVorname]} ${it[Customers.anschriftName]}' " +
                    "from company '${it[Customers.anschriftFirma]}'")
        }
    }
}

private fun insertExampleCustomer() = transaction {
    Customers.insertUnique {
        it[matchcode] = "my-lexerix-customer"
        it[anschriftFirma] = "Test"
        it[anschriftName] = "Doe"
        it[anschriftVorname] = "John"
    }
}

private fun deleteCustomer(mId: EntityID<Int>) = transaction {
    Customers.deleteWhere { id eq mId }
}

private fun customQuery() {
    // Get company type/description of first company from BH_FIRMA table
    val companyType = transaction {
        exec("SELECT TOP 1 * FROM F2.F1.BH_FIRMA") {
            it.next()
            it.getString("szUnternehmensart")
        }
    }
    println("First accounting company description: '$companyType'")
}

private fun listAllProducts() {
    transaction {
        Products.selectAll().forEach {
            println("- Product: ${it[Products.bezeichnung]}")
        }
    }
}

private fun listProductsWithPrices() {
    transaction {
        Products.withPricesAndStocks().andWhere { Products.webShop eq true }.forEach {
            println("Found product ${it[Products.bezeichnung]} with price ${it[PriceMatrix.price]} " +
                    "and stock ${it[ProductStocks.stockAmount]}")
        }
    }
}

private fun listOrdersWithProducts() {
    transaction {
        Orders.selectWithProducts().forEach { (id, contents) ->
            println("Order $id:")
            contents.forEach { product ->
                println(" - ${product[OrderContents.count]} x ${product[Products.bezeichnung]}")
            }
        }
    }
}

private fun main(): Unit = runBlocking {
    exampleSetup()

    listOrdersWithProducts()

    listAllProducts()
    listProductsWithPrices()

    listAllCustomers()

    val id = insertExampleCustomer()
    println("Created example customer.")
    listAllCustomers()

    delay(1.seconds)

    val count = deleteCustomer(id)
    assert(count == 1)
    println("Deleted example customer.")

    customQuery()
}