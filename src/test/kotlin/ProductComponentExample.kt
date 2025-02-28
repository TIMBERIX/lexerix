import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import toys.timberix.lexerix.api.inventory_management.ProductComponents
import toys.timberix.lexerix.api.inventory_management.Products

private fun main() {
    exampleSetup()

    val randomProductNr = transaction {
        val lastProduct = Products.selectAll().toList().random()
        val nr = lastProduct[Products.productNr]
        val name = lastProduct[Products.matchcode]

        println("First product number: $nr")
        println("Product name: $name")
        nr
    }

    transaction {
        ProductComponents.getForProduct(randomProductNr)
    }.forEach { (childNr, amount) ->
        println(" - $childNr: $amount")
    }

    // for all
//    transaction {
//        (Products.innerJoin(ProductComponents, onColumn = { productNr }, otherColumn = { baseProductNr})).selectAll().forEach {
//            val baseProductNr = it[ProductComponents.baseProductNr]
//            val childProductNr = it[ProductComponents.childProductNr]
//            val amount = it[ProductComponents.amount]
//            println("Base product: $baseProductNr, child product: $childProductNr, amount: $amount")
//        }
//    }
}
