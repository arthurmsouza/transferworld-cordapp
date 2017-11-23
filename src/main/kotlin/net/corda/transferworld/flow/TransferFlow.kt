package net.corda.transferworld.flow


import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.*
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.transferworld.contract.TRANSFER_CONTRACT_ID
import net.corda.transferworld.contract.TransferContract
import net.corda.transferworld.state.TransferState


// Flow.
@InitiatingFlow
@StartableByRPC
class TransferFlow(val target: Party,
                   val amount: Double) : FlowLogic<SignedTransaction>() {

    override val progressTracker: ProgressTracker = tracker()

    companion object {
        object CREATING : ProgressTracker.Step("Creating a new transfer amount")
        object SIGNING : ProgressTracker.Step("Verifying the transfer amount")
        object VERIFYING : ProgressTracker.Step("Verifying the transfer amount")
        object FINALISING : ProgressTracker.Step("Sending the transfer amount") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(CREATING, SIGNING, VERIFYING, FINALISING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        progressTracker.currentStep = CREATING

        val me = serviceHub.myInfo.legalIdentities.first()
        val notary = serviceHub.networkMapCache.notaryIdentities.single()
        val command = Command(TransferContract.Send(), listOf(me.owningKey))
        val state = TransferState(me, target, amount)
        val stateAndContract = StateAndContract(state, TRANSFER_CONTRACT_ID)
        val utx = TransactionBuilder(notary = notary).withItems(stateAndContract, command)

        progressTracker.currentStep = SIGNING
        val stx = serviceHub.signInitialTransaction(utx)

        progressTracker.currentStep = VERIFYING
        stx.verify(serviceHub)

        progressTracker.currentStep = FINALISING
        return subFlow(FinalityFlow(stx, FINALISING.childProgressTracker()))
    }
}
