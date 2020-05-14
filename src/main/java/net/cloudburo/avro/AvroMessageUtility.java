package net.cloudburo.avro;

import net.cloudburo.avro.registry.SchemaRegistry;
import net.cloudburo.avro.registry.SchemaRegistryFactory;
import org.apache.avro.Schema;
import org.apache.avro.SchemaNormalization;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AvroMessageUtility {

    public static byte[] createSingleObjectEncodingAvroMessage(Schema schema, SpecificRecord record) throws IOException {
        SchemaRegistry registry = SchemaRegistryFactory.getSchemaRegistry(SchemaRegistryFactory.registryFileBased);
        return produceBinaryAvroMessage(record,schema);
    }

    private static byte[] produceBinaryAvroMessage(SpecificRecord record, Schema schema) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(generateAvroSingleObjectEncodingHeader( SchemaNormalization.parsingFingerprint64(schema)).array());
        BinaryEncoder encoder = EncoderFactory.get().directBinaryEncoder(baos, null);
        GenericDatumWriter<Object> writer = new GenericDatumWriter<>(schema);
        writer.write(record,encoder);
        encoder.flush();
        return baos.toByteArray();
    }

    private static ByteBuffer generateAvroSingleObjectEncodingHeader(long fingerprint) {
        byte[] avroMarker = new byte[] { (byte)0xc0, (byte) 0x01 };
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES+2);
        buffer.put(avroMarker);
        buffer.putLong(fingerprint);
        return buffer;
    }
}
