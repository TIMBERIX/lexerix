import toys.timberix.lexerix.api.Lexerix

fun exampleSetup() {
    val lexerix = Lexerix()

    // This will potentially start the company database
    lexerix.connect(
        System.getenv("LEXWARE_COMPANY"),
        System.getenv("LEXWARE_IP")!!,
        System.getenv("LEXWARE_USER")!!,
        System.getenv("LEXWARE_PASSWORD")!!
    )
}