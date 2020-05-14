package net.cloudburo.avro.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
        String avroJSONSchema = schema.toString(true);
        String doc = "{";
        doc+= "\"namespace\":"+"\""+schema.getNamespace()+"."+schema.getName()+"\",";
        doc+= "\"avroschema\":"+avroJSONSchema;
        doc+= "}";
        esManager.createUpdateDocument(esIndex,"schema",doc,Long.valueOf(fingerprint).toString());
        return fingerprint;
    }

    public  Schema getSchema(long fingerprint) throws IOException {
        JsonObject jsonDoc = esManager.readDocumentByIdAsObject(esIndex, Long.valueOf(fingerprint).toString());
        String json = jsonDoc.get("_source").getAsJsonObject().get("avroschema").toString();
        Schema schema = new Schema.Parser().parse(json);
        return schema;
    }

}
