import business.model.strategy.BusinessModelStrategy;
import net.cloudburo.avro.AvroMessageUtility;
import net.cloudburo.avro.registry.SchemaRegistry;
import net.cloudburo.elasticsearch.ESPersistencyManager;
import org.apache.avro.Schema;

import java.io.IOException;
import java.util.UUID;

public class ServiceComponent {

    private static ServiceComponent serviceComponent;
    private ESPersistencyManager esPersistencyManager;
    private SchemaRegistry schemaRegistry;
    private Schema serviceComponentSchema;
    private long serviceComponentFingerprint;

    public static ServiceComponent createSingleton(ESPersistencyManager esm, SchemaRegistry registry, long fingerprint) throws IOException {
        if (serviceComponent == null) {
            serviceComponent = new ServiceComponent(esm, registry, fingerprint);
        }
        return serviceComponent;
    }

    public ServiceComponent(ESPersistencyManager esm, SchemaRegistry registry, long fingerprint) throws IOException {
        this.esPersistencyManager = esm;
        this.schemaRegistry = registry;
        this.serviceComponentFingerprint = fingerprint;
        this.serviceComponentSchema = this.schemaRegistry.getSchema(fingerprint);
        this.serviceComponentFingerprint = registry.registerSchema(serviceComponentSchema);
    }

    public byte[] processInputData(BusinessModelStrategy strategy) throws IOException {
        // =============================================================
        // Mandatory Conventions for the DAO Objects
        // =============================================================

        // Each DAO has an attribute 'fingerprint' which stores the unique schema fingerprint.
        // It can be used to fetch at any time the corresponding schema, i.e. the DAO is fully self describing!
        strategy.setAvroFingerprint(serviceComponentFingerprint);
        // Each DAO has an attribute 'indexIpid' which will be set with a UID according to our rule
        // This id will be used by ElasticSearch as the unique primary identitfier (_id attribute)
        String id  = "80008000"+ UUID.randomUUID().toString();
        strategy.setIndexIpid(id);
        // Each DAO has an attribute 'lastUpdateloginId' which is the loginId of the user who initiated the CUD operation
        strategy.setLastUpdateLoginid("jamesb");
        // Each DAO has an attribute 'lastUpdateTimestamp' which is the timestamp of the CUD operation
        strategy.setLastUpdateTimestamp(java.lang.System.currentTimeMillis());
        // Now we create from the DAO a single object encoding binary Avro message
        // This is a compact and fast format, which has the Avor schema fingerprint sealed in
        return AvroMessageUtility.createSingleObjectEncodingAvroMessage(serviceComponentSchema, strategy);
    }
}
