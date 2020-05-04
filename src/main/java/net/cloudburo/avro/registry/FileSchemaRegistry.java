package net.cloudburo.avro.registry;

import org.apache.avro.Schema;
import org.apache.avro.SchemaNormalization;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileSchemaRegistry extends SchemaRegistry {

    private final static String registryLocation = "schemaRegistry";

    public  long registerSchema(Schema schema) throws IOException {
        long fingerprint = SchemaNormalization.parsingFingerprint64(schema);
        String fiName = Long.valueOf(fingerprint).toString()+"."+schema.getNamespace()+"."+schema.getName()+".avsc";
        // An Avro Schema File with a Fingerprint is immutable
        if (!new File(fiName).exists()) {
            PrintWriter out = new PrintWriter(registryLocation + "/" + fiName);
            out.println(schema.toString(true));
            out.close();
        }
        return fingerprint;
    }

    public  Schema getSchema(long fingerprint) throws IOException {
        File  dir = new File(registryLocation);
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(Long.toString(fingerprint));
            } });
        assert(files.length ==1);
        String content = Files.readString(files[0].toPath(), StandardCharsets.UTF_8);
        Schema schema = new Schema.Parser().parse(content);
         return schema;
    };
}
