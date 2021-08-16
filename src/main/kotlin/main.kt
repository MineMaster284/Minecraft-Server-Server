import com.minerofmillions.mcserver.MCServerServer
import com.minerofmillions.mcserver.readVarInt
import com.minerofmillions.mcserver.writeVarInt
import com.moandjiezana.toml.Toml
import com.moandjiezana.toml.TomlWriter
import com.sun.xml.internal.ws.util.StreamUtils
import java.io.File
import java.util.*

val serverStartingMessage = ClassLoader.getSystemResourceAsStream("description.packet")!!.readBytes()

fun main() {
    val configFile = File("config.toml")
    if (!configFile.exists()) {
        TomlWriter().write(MCServerServer.DEFAULT_CONFIG, configFile)
        println("Default config generated: change before relaunching.")
        return
    }

    val config = Toml().read(configFile).to(MCServerServer.Config::class.java)
    val serverServer = MCServerServer(config)
    println("Starting listeners on ports: ${ serverServer.servers.map { it.serverPort } }")
    println("Type \"quit\" to end the program.")
    serverServer.start()

    while (true) {
        val line = readLine()
        if (line == null || line.startsWith("quit")) {
            break
        }
    }

    serverServer.stop()
}
