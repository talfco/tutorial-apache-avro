import business.model.strategy.BusinessModelStrategy;
import net.cloudburo.avro.AvroMessageUtility;
import net.cloudburo.avro.registry.ElasticSearchSchemaRegistry;
import net.cloudburo.avro.registry.SchemaRegistry;
import net.cloudburo.avro.registry.SchemaRegistryFactory;
import net.cloudburo.elasticsearch.ESPersistencyManager;
import org.apache.avro.Schema;
import java.io.*;
import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * The example outlines  the "creation" of a new Data Object, which will be peristed on in the ElasticSearch
 * index. So the Avro Message will consist of exactly 1 record and not an array ob records
 */
public class HelloWorld {

    private static Logger logger = Logger.getLogger(HelloWorld.class);

    public static void main(String[] args) throws IOException {
        if (args.length !=3) {
            logger.error("Expect 3 arguments: ElasticSearchServerURI, userName, password");
            return;
        }

        // ==================================
        // Connect to our Elastic Cluster and create the indexes
        // In the businessModel index we store all business model relevant document types
        // In the avroSchema index we store all Avro Schema versions
        // ==================================
        ESPersistencyManager esPersistencyManager = ESPersistencyManager.createSingleton(args[0], args[1],args[2]);
        if (!esPersistencyManager.existsIndex("businessmodel")) {
            esPersistencyManager.createIndex("businessmodel");
        }
        if (!esPersistencyManager.existsIndex("avroschema")) {
            esPersistencyManager.createIndex("avroschema");
        }

        // ==============================================================================================
        // Registration of our Schema (Data Interface Contract) in an Avro Schema Registry.
        // The Schema Registry will make sure to create a unique fingerprint, which is a version identifier of the schema
        // The Avro Schema Fingerprint is crucial to decouple producer and consumer of an Avro Message
        // ============================================================================================
        ElasticSearchSchemaRegistry registry = (ElasticSearchSchemaRegistry)SchemaRegistryFactory.getSchemaRegistry(SchemaRegistryFactory.registryElasticSearchBased);
        registry.setESPersistencyManager(esPersistencyManager);
        // Fetch the fingerprint UID of the used schema, if doesn't exists yet, it will be newly registered
        Schema schema = new Schema.Parser().parse(new File("src/main/avro/businessModel-strategy.avsc"));
        long schemaFingerprint = registry.registerSchema(schema);

        // Let's create our BackendComponent which will manage the persistency layer
        BackendComponent beComponent = BackendComponent.createSingleton(esPersistencyManager);

        // Create a BusinessModel Strategy Object / Test Data
        // The BusinessModelStrategy object is a Data Access Object generated by the Avro Schema Generator
        // Avro doesn't require  generated Data Access Objects and offers a generic way (if required)
        BusinessModelStrategy strategy = TestLoader.getBusinessModelStrategy();

        // =============================================================
        // Mandatory Conventions for the DAO Objects
        // =============================================================

        // Each DAO has an attribute 'fingerprint' which stores the unique schema fingerprint.
        // It can be used to fetch at any time the corresponding schema, i.e. the DAO is fully self describing!
        strategy.setAvroFingerprint(schemaFingerprint);
        // Each DAO has an attribute 'indexIpid' which will be set with a UID according to our rule
        // This id will be used by ElasticSearch as the unique primary identitfier (_id attribute)
        String id  = "80008000"+UUID.randomUUID().toString();
        strategy.setIndexIpid(id);
        // Each DAO has an attribute 'lastUpdateloginId' which is the loginId of the user who initiated the CUD operation
        strategy.setLastUpdateLoginid("jamesb");
        // Each DAO has an attribute 'lastUpdateTimestamp' which is the timestamp of the CUD operation
        strategy.setLastUpdateTimestamp(java.lang.System.currentTimeMillis());
        // Now we create from the DAO a single object encoding binary Avro message
        // This is a compact and fast format, which has the Avor schema fingerprint sealed in
        byte[] msg = AvroMessageUtility.createSingleObjectEncodingAvroMessage(schema, strategy);

        // We pass the binary message to our backend component, in a real example this may be
        // a JSON, Webservice or RPC call, in our sample a simple library call
        String payloadJson = beComponent.persist(msg, "businessmodel", "strategy", strategy.getIndexIpid().toString());
        logger.info("Persisted Avro payload JSON: "+payloadJson);

        //String mappingJson = ElasticSearchMapper.convertToESMapping("employee","profile",schema);
        //logger.info("ES Mapping JSON"+mappingJson);
        //JestClient cli = JestDemoApplication.jestClient(args[0], args[1],args[2]);
        //System.out.println("Got ElasticSearch Connection "+cli.toString());
        //JestDemoApplication.createUpdateIndex(cli,"employee","profile", mappingJson);
        //JestDemoApplication.createDoc(cli,payloadJson);
    }
}
