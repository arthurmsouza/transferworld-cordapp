package net.corda.transferworld.plugin


import net.corda.transferworld.api.TransferApi
import net.corda.webserver.services.WebServerPluginRegistry
import java.util.function.Function



class TransferWebPlugin : WebServerPluginRegistry {
    override val webApis = listOf(Function(::TransferApi))
    override val staticServeDirs = mapOf("transferworld" to javaClass.classLoader.getResource("transferworldWeb").toExternalForm())
}