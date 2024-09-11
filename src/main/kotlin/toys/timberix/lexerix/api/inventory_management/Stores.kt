@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package toys.timberix.lexerix.api.inventory_management

import toys.timberix.lexerix.api.utils.DatedIntIdTable

object Stores : DatedIntIdTable("FK_Lager", "lId") {
    val name = varchar("Name", 30)
    val inactive = bool("bInaktiv").default(false)
}