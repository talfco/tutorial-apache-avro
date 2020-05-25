package net.cloudburo.avro.registry;

import org.apache.avro.Schema;
import org.apache.avro.SchemaNormalization;
import java.io.IOException;


public abstract class SchemaRegistry {

    public  abstract long registerSchema(Schema schema) throws IOException;
    public  abstract Schema getSchema(long fingerprint) throws IOException;

    public long getSchemaFingerprint(Schema schema) {
        return SchemaNormalization.parsingFingerprint64(schema);
    }
}
