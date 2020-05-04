package net.cloudburo.avro.registry;



public class SchemaRegistryFactory {

    public static final String registryFileBased = "RF";

    public static FileSchemaRegistry getSchemaRegistry(String type) {
        if (registryFileBased.endsWith("RF"))
            return new FileSchemaRegistry();
        else
            return null;
    }


}
