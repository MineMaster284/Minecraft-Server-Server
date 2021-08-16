package com.minerofmillions.mcserver

import java.io.File

class MCServerServer(config: Config) {
    internal val servers = config.servers.map { MCServerListener(it) }
    private val serverThreads = servers.map { Thread(it) }
    private val threadCheckThread = Thread {
        while (serverThreads.any { it.isAlive }) {
            if (serverThreads.any { !it.isAlive }) {
                stop()
                break
            }
        }
    }.apply {
        isDaemon = true
        start()
    }

    fun start() {
        serverThreads.forEach { it.start() }
    }

    fun stop() {
        servers.forEach { it.stop() }
    }

    class Config(val servers: Set<MCServerListener.Metadata>) {
        override fun toString(): String = servers.joinToString { File(it.baseDirectory, it.forgeServerFileName).path }
    }

    companion object {
        val DEFAULT_CONFIG = Config(setOf(MCServerListener.Metadata("path/to/your/server/folder", "2G", "6G", "forge-server.jar", "java")))
    }
}
