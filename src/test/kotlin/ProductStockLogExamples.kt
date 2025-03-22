import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import toys.timberix.lexerix.api.inventory_management.*

private fun main() {
    exampleSetup()

    // Get a product and a store to use in the example
    val (product, store) = transaction {
        val product = Products.selectAll().first()
        val store = Stores.selectAll().first()
        Pair(product, store)
    }

    println("Using product ${product[Products.bezeichnung]} (${product[Products.productNr]})")
    println("Using store ${store[Stores.name]} (${store[Stores.id]})")

    // Get the highest id before inserting new entries
    val highestId = transaction {
        ProductStockLog.selectAll().maxOfOrNull { it[ProductStockLog.id] } ?: 0
    }
    println("Highest id before insertion: $highestId")

    // Insert first entry and verify its id
    val firstId = insertStockLogEntry(product, store)
    println("First entry id: $firstId, expected: ${highestId + 1}")
    assert(firstId == highestId + 1) { "First entry id $firstId is not equal to expected ${highestId + 1}" }

    // Insert second entry and verify its id is consecutive
    val secondId = insertStockLogEntry(product, store, "Second test entry")
    println("Second entry id: $secondId, expected: ${firstId + 1}")
    assert(secondId == firstId + 1) { "Second entry id $secondId is not consecutive to first id $firstId" }
}

private fun insertStockLogEntry(
    product: org.jetbrains.exposed.sql.ResultRow, 
    store: org.jetbrains.exposed.sql.ResultRow,
    customDescription: String? = null
): Int {
    val id = transaction {
        ProductStockLog.insertUnique { it ->
            // Set the product and store
            it[productNr] = product[Products.productNr]
            it[ProductStockLog.store] = store[Stores.id]

            // Set the type (3 for manual stock change)
            it[type] = 3

            // Set the description and user
            it[description] = customDescription ?: "Manual stock adjustment for ${product[Products.bezeichnung]}"
            it[user] = "Example User"

            // Set the stock difference and new stock level
            val difference = 10
            it[ProductStockLog.difference] = difference

            // Calculate the new stock level
            // In a real application, you would get the current stock level from ProductStocks
            val currentStock = 0 // Placeholder, in a real app you would query this
            it[stockAfter] = currentStock + difference
        }
    }

    println("Inserted stock log entry with id $id")
    return id
}
