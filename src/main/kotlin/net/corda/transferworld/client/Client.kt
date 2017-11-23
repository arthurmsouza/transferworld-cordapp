package net.corda.transferworld.client

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.loggerFor
import net.corda.transferworld.state.TransferState
import org.slf4j.Logger

fun main(args: Array<String>) {
    TransferWorldRPC().main(args)
}

private class TransferWorldRPC {
    companion object {
        val logger: Logger = loggerFor<TransferWorldRPC>()
    }

    fun main(args: Array<String>) {
        require(args.size == 1) { "Usage: TransferWorldRPC <node address:port>" }
        val nodeAddress = NetworkHostAndPort.parse(args[0])
        val client = CordaRPCClient(nodeAddress)
        // Can be amended in the com.template.MainKt file.
        val proxy = client.start("user1", "test").proxy
        // Grab all signed transactions and all future signed transactions.
        val (transactions, futureTransactions) = proxy.internalVerifiedTransactionsFeed()
        // Log the existing transfers and listen for new ones.
        futureTransactions.startWith(transactions).toBlocking().subscribe { transaction ->
            transaction.tx.outputs.forEach { output ->
                val state = output.data as TransferState
                logger.info(state.toString())
            }
        }
    }
}
