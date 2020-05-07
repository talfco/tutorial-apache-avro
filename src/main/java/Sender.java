
import example.avro.User;
import net.cloudburo.avro.elasticsearch.ElasticSearchMapper;
import net.cloudburo.avro.registry.SchemaRegistry;
import net.cloudburo.avro.registry.SchemaRegistryFactory;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Sender {

    public static byte[] sendUserMessage (Schema schema) throws IOException {
        SchemaRegistry registry = SchemaRegistryFactory.getSchemaRegistry(SchemaRegistryFactory.registryFileBased);
        long fingerprint = registry.registerSchema(schema);
        String elasticSearchMapping = ElasticSearchMapper.convertToESMapping(schema);
        System.out.println("ES Mapping "+elasticSearchMapping);
        return produceBinaryAvroMessage(TestLoader.getUser(),schema, fingerprint);
    }

    /**
     * @param record An Avro Java Data Object
     * @param schema The Avro Schema associate with the Avro Java OBject
     * @return
     * @throws IOException
     */
    public static byte[] produceBinaryAvroMessage(User record, Schema schema, long fingerprint) throws IOException {
        record.setAvroFingerprint(fingerprint);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(generateAvroSingleObjectEncodingHeader(schema,fingerprint).array());
        BinaryEncoder encoder = EncoderFactory.get().directBinaryEncoder(baos, null);
        GenericDatumWriter<Object> writer = new GenericDatumWriter<>(schema);
        writer.write(record,encoder);
        encoder.flush();
        return baos.toByteArray();
    }

    public static ByteBuffer generateAvroSingleObjectEncodingHeader(Schema schema, long fingerprint) {
        byte[] avroMarker = new byte[] { (byte)0xc0, (byte) 0x01 };
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES+2);
        buffer.put(avroMarker);
        buffer.putLong(fingerprint);
        return buffer;
    }

}
