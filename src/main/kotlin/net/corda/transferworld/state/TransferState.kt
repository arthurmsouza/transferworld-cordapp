package net.corda.transferworld.state


import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table


// State.
data class TransferState(val origin: Party,
                         val target: Party,
                         val amount: Double) : ContractState, QueryableState {
    override val participants get() = listOf(target)
    override fun toString() = "${origin.name}: $amount"
    override fun supportedSchemas() = listOf(TransferSchemaV1)
    override fun generateMappedObject(schema: MappedSchema) = TransferSchemaV1.PersistentTransferState(
            origin.name.toString(), target.name.toString(), amount)

    object TransferSchema

    object TransferSchemaV1 : MappedSchema(TransferSchema.javaClass, 1, listOf(PersistentTransferState::class.java)) {
        @Entity
        @Table(name = "transfers")
        class PersistentTransferState(
                @Column(name = "origin")
                var origin: String = "",
                @Column(name = "target")
                var target: String = "",
                @Column(name = "amount")
                var amount: Double = 0.0
        ) : PersistentState()
    }
}

