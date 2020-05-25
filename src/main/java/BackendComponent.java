import net.cloudburo.avro.registry.SchemaRegistry;
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
    private Schema backendComponentSchema;
    private long backendComponentSchemaFingerprint;

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
        backendComponentSchema = new Schema.Parser().parse(new File("src/main/avro/businessModel-strategy.avsc"));
        this.backendComponentSchemaFingerprint = registry.registerSchema(backendComponentSchema);
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
            String jsonDoc = convertAvroBinaryToJSON(msg);
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

    private  String convertAvroBinaryToJSON(byte[] msg)
            throws IOException {
        // We retrieve the fingerprint of the message

        long msgFingerprint = getAvroFingerprint(msg);
        // Now we retrieve the Schema via our registry
        Schema mgsSchema = registry.getSchema(msgFingerprint);
        byte[] payload = extractPayload(msg);
        // Here comes the magic of Schema Evolution
        // We are checking if received fingerprint is the same of our backend component
        // If not, we pass in both Schemas to the GenericDatumReader which transform
        // the message to our
        DatumReader<Object> reader = null;
        if (msgFingerprint == this.backendComponentSchemaFingerprint)
            reader = new GenericDatumReader<>(mgsSchema);
        else
            reader = new GenericDatumReader<>(mgsSchema,this.backendComponentSchema);

        DatumWriter<Object> writer = new GenericDatumWriter<>(mgsSchema);

        BinaryDecoder binaryDecoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(payload), null);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonEncoder jsonEncoder = EncoderFactory.get().jsonEncoder(mgsSchema, outputStream, true);
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
