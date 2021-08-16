package com.minerofmillions.mcserver

data class MCPacket(val packetId: Int, val data: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MCPacket

        if (packetId != other.packetId) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packetId
        result = 31 * result + data.contentHashCode()
        return result
    }

    fun toByteArray(): ByteArray {
        val tmp = mutableListOf<Byte>()
        writeVarInt(packetId, tmp)
        tmp.addAll(data.toList())
        val output = mutableListOf<Byte>()
        writeVarInt(tmp.size, output)
        output.addAll(tmp)
        return output.toByteArray()
    }
}
