package net.cloudburo.avro.registry;

import net.cloudburo.elasticsearch.ESPersistencyManager;
import org.apache.avro.Schema;

import java.io.IOException;

public class ElasticSearchSchemaRegistry extends SchemaRegistry {

    private static final String esIndex = "avroschema";

    ESPersistencyManager esManager;

    public void setESPersistencyManager(ESPersistencyManager esManager) {
        this.esManager = esManager;
    }

    public  long registerSchema(Schema schema) throws IOException {
        long fingerprint = getSchemaFingerprint(schema);
        esManager.createUpdateDocument(esIndex,"schema",schema.toString(false),Long.valueOf(fingerprint).toString());
        return fingerprint;
    }

    public  Schema getSchema(long fingerprint) throws IOException {
        String jsonDoc = esManager.readDocumentById(esIndex, Long.valueOf(fingerprint).toString());
        Schema schema = new Schema.Parser().parse(jsonDoc);
        return schema;
    }

}
