@file:Suppress("ClassName")

package toys.timberix

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import toys.timberix.toys.timberix.lexerix.api.Lexerix
import toys.timberix.toys.timberix.lexerix.api.inventory_management.InventoryManagement
import toys.timberix.toys.timberix.lexerix.api.inventory_management.InventoryManagement.Customers.anschriftFirma
import toys.timberix.toys.timberix.lexerix.api.inventory_management.InventoryManagement.Customers.anschriftName
import toys.timberix.toys.timberix.lexerix.api.inventory_management.InventoryManagement.Customers.anschriftVorname
import toys.timberix.toys.timberix.lexerix.api.inventory_management.InventoryManagement.Products.bezeichnung
import toys.timberix.toys.timberix.lexerix.api.inventory_management.InventoryManagement.Products.created
import toys.timberix.toys.timberix.lexerix.api.inventory_management.InventoryManagement.Products.createdUser
import toys.timberix.toys.timberix.lexerix.api.inventory_management.InventoryManagement.Products.lastUpdated
import toys.timberix.toys.timberix.lexerix.api.inventory_management.InventoryManagement.Products.preis
import kotlin.time.Duration.Companion.seconds

fun listAllCustomers() {
    transaction {
        println("Customers:")
        InventoryManagement.Customers.selectAll().forEach {
            println(" - '${it[anschriftVorname]} ${it[anschriftName]}' from company '${it[anschriftFirma]}'")
        }
    }
}

fun insertExampleCustomer() = transaction {
    InventoryManagement.Customers.insertUnique {
        it[matchcode] = "my-lexerix-customer"
        it[anschriftFirma] = "Test"
        it[anschriftName] = "Doe"
        it[anschriftVorname] = "John"
    }
}

fun deleteCustomer(mId: EntityID<Int>) = transaction {
    InventoryManagement.Customers.deleteWhere { id eq mId }
}

fun customQuery() {
    // Get company type/description of first company from BH_FIRMA table
    val companyType = transaction {
        exec("SELECT TOP 1 * FROM F2.F1.BH_FIRMA") {
            it.next()
            it.getString("szUnternehmensart")
        }
    }
    println("First accounting company description: '$companyType'")
}

fun listAllProducts() {
    transaction {
        InventoryManagement.Products.selectAll().forEach {
            println("Product: ${it[bezeichnung]} (${it[preis]}) - created by ${it[createdUser]} at ${it[created]}")
            println("   -> Last modified at ${it[lastUpdated]} (GMT)")
        }
    }
}

private fun main(): Unit = runBlocking {
    val lexerix = Lexerix()

    // This will potentially start the company database
    lexerix.connect(
        System.getenv("LEXWARE_COMPANY"),
        System.getenv("LEXWARE_IP")!!,
        System.getenv("LEXWARE_USER")!!,
        System.getenv("LEXWARE_PASSWORD")!!
    )

    listAllProducts()

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