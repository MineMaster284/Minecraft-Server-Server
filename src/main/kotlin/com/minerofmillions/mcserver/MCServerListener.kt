package com.minerofmillions.mcserver

import serverStartingMessage
import java.io.File
import java.net.ServerSocket
import java.net.SocketException
import java.util.*
import java.util.concurrent.TimeUnit

class MCServerListener(private val metadata: Metadata) : Runnable {
    private val baseDirectory = File(metadata.baseDirectory)
    private val serverProperties = Properties().apply {
        File(baseDirectory, "server.properties").reader().use(this::load)
    }

    private var running = true

    private var portListener: ServerSocket? = null
    private var serverProcess: Process? = null

    data class Metadata(val baseDirectory: String, val minAllocatedRam: String, val maxAllocatedRam: String, val forgeServerFileName: String, val javaVersion: String)

    val serverPort = serverProperties.getProperty("server-port").toInt()

    override fun run() {
        try {
            while (running) {
                println("Listening on port $serverPort.")
                portListener = ServerSocket(serverPort)
                portListener!!.accept().use { clientConnection ->
                    clientConnection.getInputStream().buffered().use { clientInput ->
                        clientConnection.getOutputStream().buffered().use { clientOutput ->
                            // Skip but verify the existence of two packets
                            val (handshakeLength, _) = readVarInt(clientInput)
                            clientInput.skip(handshakeLength.toLong())
                            val (requestLength, _) = readVarInt(clientInput)
                            clientInput.skip(requestLength.toLong())

                            // Send pre-determined "Server Starting Up" response
                            clientOutput.write(serverStartingMessage)
                            clientOutput.flush()

                            // Send Ping Pong packet
                            clientOutput.write(readPacket(clientInput).toByteArray())
                        }
                    }
                }

                portListener!!.close()
                portListener = null

                println("Starting server on port $serverPort.")
                serverProcess = ProcessBuilder(metadata.javaVersion, "-Xmx${metadata.maxAllocatedRam}", "-Xms${metadata.minAllocatedRam}", "-jar", metadata.forgeServerFileName)
                    .directory(baseDirectory)
                    .redirectOutput(File("output.$serverPort.log"))
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start()
                println("Process started.")
                serverProcess!!.waitFor()
                serverProcess = null
            }
        } catch (e: SocketException) {
            println("Server on port $serverPort shut down.")
        }
        portListener?.close()
    }

    fun stop() {
        println("Stopping server on port $serverPort.")
        running = false
        portListener?.close()
        serverProcess?.outputStream?.writer()?.use { it.write("stop") }
        if (serverProcess?.waitFor(2, TimeUnit.MINUTES) == false) serverProcess?.destroyForcibly()
    }
}
