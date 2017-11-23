package net.corda.transferworld.contract

import net.corda.testing.*
import net.corda.testing.contracts.DUMMY_PROGRAM_ID
import net.corda.testing.contracts.DummyState
import net.corda.transferworld.state.TransferState
import org.junit.After
import org.junit.Before
import org.junit.Test

class TransferContractTests {
    @Before
    fun setup() {
        setCordappPackages("net.corda.transferworld.contract", "net.corda.testing.contracts")
    }

    @After
    fun tearDown() {
        unsetCordappPackages()
    }

    @Test
    fun transferTransactionMustBeWellFormed() {
        // A pre-made Transfer to Bob.
        val transfer = TransferState(ALICE, BOB,50.0)
        // Tests.
        ledger {
            // Input state present.
            transaction {
                input(DUMMY_PROGRAM_ID) { DummyState() }
                command(ALICE_PUBKEY) { TransferContract.Send() }
                output(TRANSFER_CONTRACT_ID) { transfer }
                this.failsWith("There can be no inputs when transfering other parties.")
            }
            // Wrong command.
            transaction {
                output(TRANSFER_CONTRACT_ID) { transfer }
                command(ALICE_PUBKEY) { DummyCommandData }
                this.failsWith("")
            }
            // Command signed by wrong key.
            transaction {
                output(TRANSFER_CONTRACT_ID) { transfer }
                command(MINI_CORP_PUBKEY) { TransferContract.Send() }
                this.failsWith("The transfer must be signed by the sender.")
            }
            // Sending to yourself is not allowed.
            transaction {
                output(TRANSFER_CONTRACT_ID) { TransferState(ALICE, ALICE, 100.0) }
                command(ALICE_PUBKEY) { TransferContract.Send() }
                this.failsWith("No sending transfer to yourself!")
            }
            transaction {
                output(TRANSFER_CONTRACT_ID) { transfer }
                command(ALICE_PUBKEY) { TransferContract.Send() }
                this.verifies()
            }
        }
    }
}