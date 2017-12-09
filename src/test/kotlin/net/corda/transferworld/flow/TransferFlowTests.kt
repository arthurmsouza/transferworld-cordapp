package net.corda.transferworld.flow

import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria.VaultCustomQueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.utilities.getOrThrow
import net.corda.node.internal.StartedNode
import net.corda.testing.node.MockNetwork
import net.corda.testing.setCordappPackages
import net.corda.testing.unsetCordappPackages
import net.corda.transferworld.state.TransferState
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals


class TransferFlowTests {
    lateinit var net: MockNetwork
    lateinit var a: StartedNode<MockNetwork.MockNode>
    lateinit var b: StartedNode<MockNetwork.MockNode>
    var amount: Double = 50.0

    @Before
    fun setup() {
        setCordappPackages("net.corda.transferworld.contract")
        net = MockNetwork()
        val nodes = net.createSomeNodes(2)
        a = nodes.partyNodes[0]
        b = nodes.partyNodes[1]
        net.runNetwork()
        amount = 100.0

        a.services.schemaService.registerCustomSchemas(setOf(TransferState.TransferSchemaV1));
        b.services.schemaService.registerCustomSchemas(setOf(TransferState.TransferSchemaV1));


    }

    @After
    fun tearDown() {
        unsetCordappPackages()
        net.stopNodes()
    }

    @Test
    fun flowWorksCorrectly() {
        val transfer = TransferState(a.info.legalIdentities.first(), b.info.legalIdentities.first(), amount)
        val flow = TransferFlow(b.info.legalIdentities.first(),amount)
        val future = a.services.startFlow(flow).resultFuture
        net.runNetwork()
        val stx = future.getOrThrow()
        // Check transferworld transaction is stored in the storage service.
        val bTx = b.services.validatedTransactions.getTransaction(stx.id)
        assertEquals(bTx, stx)
        print("bTx == $stx\n")
        // Check transferworld state is stored in the vault.
        b.database.transaction {
            // Simple query.
            val bTransfer = b.services.vaultService.queryBy<TransferState>().states.single().state.data
            assertEquals(bTransfer.toString(), transfer.toString())
            print("$bTransfer == $transfer\n")
            // Using a custom criteria directly referencing schema entity attribute.
            val expression = builder { TransferState.TransferSchemaV1.PersistentTransferState::amount.greaterThan(0.0) }
            val customQuery = VaultCustomQueryCriteria(expression)
            val bTransfer2 = b.services.vaultService.queryBy<TransferState>(customQuery).states.single().state.data
            assertEquals(bTransfer2.amount, transfer.amount)
            print("$bTransfer2 == $transfer\n")
        }
    }
}