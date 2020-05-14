package net.cloudburo.avro.registry;

public class SchemaRegistryFactory {

    public static final String registryFileBased = "RF";
    public static final String registryElasticSearchBased = "ES";

    public static SchemaRegistry getSchemaRegistry(String type) {
        if (type.equals(registryFileBased))
            return new FileSchemaRegistry();
        if (type.equals(registryElasticSearchBased))
            return new ElasticSearchSchemaRegistry();
        else
            return null;
    }

}
