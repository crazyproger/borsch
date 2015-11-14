package net.crazyproger.borsch.rpc

import io.grpc.Metadata
import java.nio.ByteBuffer

object MetadataKeys {
    val secretId: Metadata.Key<String> = Metadata.Key.of("rpc-secret-id", Metadata.ASCII_STRING_MARSHALLER)
    val businessErrorCode: Metadata.Key<Int> = Metadata.Key.of("rpc-error-code-bin", IntBinaryMarshaller)
}

object IntBinaryMarshaller : Metadata.BinaryMarshaller<Int> {
    override fun toBytes(value: Int?): ByteArray? {
        return ByteBuffer.allocate(4).putInt(value ?: 0).array()
    }

    override fun parseBytes(serialized: ByteArray?): Int? {
        if (serialized == null || serialized.isEmpty()) return 0
        return ByteBuffer.wrap(serialized).int
    }

}