package net.corda.transferworld.contract

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import net.corda.transferworld.state.TransferState



// Contract.
const val TRANSFER_CONTRACT_ID = "net.corda.transferworld.contract.TransferContract"

class TransferContract: Contract {

    // Command.
    class Send : TypeOnlyCommandData()

    // Contract code.
    override fun verify(tx: LedgerTransaction) = requireThat {
        val command = tx.commands.requireSingleCommand<Send>()
        "There can be no inputs when transfering other parties." using (tx.inputs.isEmpty())
        "There must be one output: The transfer" using (tx.outputs.size == 1)
        val transfer = tx.outputsOfType<TransferState>().single()
        "No sending transfer to yourself!" using (transfer.target != transfer.origin)
        "The transfer must be signed by the sender." using (transfer.origin.owningKey == command.signers.single())
    }
}