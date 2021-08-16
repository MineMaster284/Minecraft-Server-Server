package com.minerofmillions.mcserver

import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.or

fun readVarInt(buffer: InputStream): Pair<Int, Int> {
    var numRead = 0
    var result = 0
    var read: Byte
    do {
        read = buffer.read().toByte()
        val value: Int = (read and 127).toInt()
        result = result or (value shl 7 * numRead)
        numRead++
        if (numRead > 5) {
            error("VarInt is too big")
        }
    } while ((read and 128.toByte()).toInt() != 0)
    return result to numRead
}
fun readVarInt(buffer: ByteArray): Pair<Int, Int> {
    var numRead = 0
    var result = 0
    var read: Byte
    do {
        read = buffer[numRead]
        val value: Int = (read and 127).toInt()
        result = result or (value shl 7 * numRead)
        numRead++
        if (numRead > 5) {
            error("VarInt is too big")
        }
    } while ((read and 128.toByte()).toInt() != 0)
    return result to numRead
}

fun readVarLong(buffer: InputStream): Pair<Long, Int> {
    var numRead = 0
    var result: Long = 0
    var read: Byte
    do {
        read = buffer.read().toByte()
        val value: Long = (read and 127).toLong()
        result = result or (value shl 7 * numRead)
        numRead++
        if (numRead > 10) {
            error("VarLong is too big")
        }
    } while ((read and 128.toByte()).toInt() != 0)
    return result to numRead
}

fun writeVarInt(v: Int, buffer: ByteBuffer) {
    var value = v
    do {
        var temp = (value and 127).toByte()
        // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
        value = value ushr 7
        if (value != 0) {
            temp = temp or 128.toByte()
        }
        buffer.put(temp)
    } while (value != 0)
}

fun writeVarInt(v: Int, buffer: MutableCollection<Byte>) {
    var value = v
    do {
        var temp = (value and 127).toByte()
        value = value ushr 7
        if (value != 0) {
            temp = temp or 128.toByte()
        }
        buffer.add(temp)
    } while (value != 0)
}

fun writeVarLong(v: Long, buffer: ByteBuffer) {
    var value = v
    do {
        var temp = (value and 127).toByte()
        // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
        value = value ushr 7
        if (value != 0L) {
            temp = temp or 128.toByte()
        }
        buffer.put(temp)
    } while (value != 0L)
}

fun readPacket(inputStream: InputStream): MCPacket {
    val (length, _) = readVarInt(inputStream)
    val (packetId, idLength) = readVarInt(inputStream)
    return MCPacket(packetId, (idLength until length).map { inputStream.read().toByte() }.toByteArray())
}

fun String.toPacketByteArray(): ByteArray {
    val me = toByteArray(Charsets.UTF_8)
    val output = mutableListOf<Byte>()
    writeVarInt(me.size, output)
    output.addAll(me.toList())
    return output.toByteArray()
}
