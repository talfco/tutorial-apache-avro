import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import net.cloudburo.avro.registry.ElasticSearchSchemaRegistry;
import net.cloudburo.avro.registry.SchemaRegistry;
import net.cloudburo.avro.registry.SchemaRegistryFactory;
import net.cloudburo.elasticsearch.ESPersistencyManager;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

public class BackendComponent {

    private static BackendComponent backendComponent;
    private ESPersistencyManager esPersistencyManager;
    private SchemaRegistry registry;
    private long avroSchemaFingerprint;

    public static BackendComponent createSingleton(ESPersistencyManager esm, SchemaRegistry registry) throws IOException {
        if (backendComponent == null) {
            backendComponent = new BackendComponent(esm, registry);
        }
        return backendComponent;
    }

    public BackendComponent(ESPersistencyManager esm, SchemaRegistry registry) throws IOException {
        this.registry = registry;
        this.esPersistencyManager = esm;
        if (!esPersistencyManager.existsIndex("businessmodel")) {
            esPersistencyManager.createIndex("businessmodel");
        }
        if (!esPersistencyManager.existsIndex("avroschema")) {
            esPersistencyManager.createIndex("avroschema");
        }
        String mappings = new String ( Files.readAllBytes( Paths.get("src/main/resources/mappings-avro-index.json") ) );
        esPersistencyManager.updateESMapping("avroschema","schema",mappings);
        // Fetch the fingerprint UID of the used schema, if doesn't exists yet, it will be newly registered
        Schema schema = new Schema.Parser().parse(new File("src/main/avro/businessModel-strategy.avsc"));
        this.avroSchemaFingerprint = registry.registerSchema(schema);
    }

    private static Logger logger = Logger.getLogger(BackendComponent.class);

    /**
     * Persist the single object encoded Avor Binary Message as a JSON Avro Message
     * @param msg
     * @return The JSON Avor essage
     * @throws IOException
     */
    public  String persist(byte[] msg, String index, String type, String id) throws IOException {
        if (checkForAvroSingleObjectEncoding(msg)) {
            long msgFingerprint = getAvroFingerprint(msg);
            Schema schema = registry.getSchema(msgFingerprint);
            byte[] payload = extractPayload(msg);
            String jsonDoc = convertAvroBinaryToJSON(payload,schema);
            esPersistencyManager.createUpdateDocument(index,type,jsonDoc,id);
            return jsonDoc;
        }
        else {
            logger.error("Received message wasn't Avro Single Object encoded");
            throw new IOException("Received message wasn't Avro Single Object encoded");
        }
    }

    private  boolean checkForAvroSingleObjectEncoding(byte[] msg) {
        return (msg[0] == (byte)0xc0 && msg[1] == (byte)0x01);
    }

    private  long getAvroFingerprint(byte[] msg) {
        byte[] fp = new byte[Long.BYTES];
        System.arraycopy(msg,2,fp,0,Long.BYTES);
        ByteBuffer byteBuffer = ByteBuffer.wrap(fp);
        long fpstr =  byteBuffer.getLong();
        return fpstr;
    }

    private  byte[] extractPayload(byte[] msg) {
        byte[] pl = new byte[msg.length-2-Long.BYTES];
        System.arraycopy(msg,10,pl,0,msg.length-2-Long.BYTES);
        return pl;
    }

    private  String convertAvroBinaryToJSON(byte[] msg, Schema schema)
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
