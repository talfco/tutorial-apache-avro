import net.cloudburo.avro.registry.SchemaRegistry;
import net.cloudburo.avro.registry.SchemaRegistryFactory;
import net.cloudburo.elasticsearch.JestDemoApplication;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

public class Receiver {

    private static Logger logger = Logger.getLogger(Receiver.class);

    public static String receive (byte[] msg) throws IOException {
        if (checkForAvroSingleObjectEncoding(msg)) {
            long fingerprint = getAvroFingerprint(msg);
            SchemaRegistry registry = SchemaRegistryFactory.getSchemaRegistry(SchemaRegistryFactory.registryFileBased);
            Schema schema = registry.getSchema(fingerprint);
            byte[] payload = extractPayload(msg);
            String jsonDoc = convertAvroBinaryToJSON(payload,schema);
            return jsonDoc;
        }
        else {
            logger.error("Received message wasn't Avro Single Object encoded");
            throw new IOException("Received message wasn't Avro Single Object encoded");
        }
    }

    private static boolean checkForAvroSingleObjectEncoding(byte[] msg) {
        return (msg[0] == (byte)0xc0 && msg[1] == (byte)0x01);
    }

    private static long getAvroFingerprint(byte[] msg) {
        byte[] fp = new byte[Long.BYTES];
        System.arraycopy(msg,2,fp,0,Long.BYTES);
        ByteBuffer byteBuffer = ByteBuffer.wrap(fp);
        long fpstr =  byteBuffer.getLong();
        return fpstr;
    }

    private static byte[] extractPayload(byte[] msg) {
        byte[] pl = new byte[msg.length-2-Long.BYTES];
        System.arraycopy(msg,10,pl,0,msg.length-2-Long.BYTES);
        return pl;
    }

    private static String convertAvroBinaryToJSON(byte[] msg, Schema schema)
            throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        DatumReader<Object> reader = new GenericDatumReader<>(schema);
        DatumWriter<Object> writer = new GenericDatumWriter<>(schema);

        BinaryDecoder binaryDecoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(msg), null);

        JsonEncoder jsonEncoder = EncoderFactory.get().jsonEncoder(schema, outputStream, true);
        Object datum = null;
        while (!binaryDecoder.isEnd()) {
            datum = reader.read(datum, binaryDecoder);
            writer.write(datum, jsonEncoder);
            jsonEncoder.flush();
        }
        outputStream.flush();
        return outputStream.toString();
    }
}
