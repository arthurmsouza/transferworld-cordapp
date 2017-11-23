package net.corda.transferworld.api

import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.getOrThrow
import net.corda.transferworld.flow.TransferFlow
import net.corda.transferworld.state.TransferState
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

// API.
@Path("transferworld")
class TransferApi(val rpcOps: CordaRPCOps) {
    @GET
    @Path("transfer")
    @Produces(MediaType.APPLICATION_JSON)
    fun transfer(@QueryParam(value = "target") target: String,
                 @QueryParam(value = "amount") amount: Double): Response {
        val (status, message) = try {
            // Look-up the 'target'.
            val matches = rpcOps.partiesFromName(target, exactMatch = true)

            // We only want one result!
            val to: Party = when {
                matches.isEmpty() -> throw IllegalArgumentException("Target string doesn't match any nodes on the network.")
                matches.size > 1 -> throw IllegalArgumentException("Target string matches multiple nodes on the network.")
                else -> matches.single()
            }

            // Start the flow, block and wait for the response.
            val result = rpcOps.startFlowDynamic(TransferFlow::class.java, to, amount).returnValue.getOrThrow()
            // Return the response.
            Response.Status.CREATED to "You just sent a amount of money to ${to.name} (Transaction ID: ${result.tx.id})"
        } catch (e: Exception) {
            Response.Status.BAD_REQUEST to e.message
        }
        return Response.status(status).entity(message).build()
    }

    @GET
    @Path("amounts")
    @Produces(MediaType.APPLICATION_JSON)
    fun amounts() = rpcOps.vaultQuery(TransferState::class.java).states

    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun me() = mapOf("me" to rpcOps.nodeInfo().legalIdentities.first().name)

    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun peers() = mapOf("peers" to rpcOps.networkMapSnapshot().map { it.legalIdentities.first().name })
}

