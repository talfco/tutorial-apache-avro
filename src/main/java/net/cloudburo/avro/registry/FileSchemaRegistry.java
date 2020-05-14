package net.cloudburo.avro.registry;

import org.apache.avro.Schema;

import java.io.*;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;

public class FileSchemaRegistry extends SchemaRegistry {

    private final static String registryLocation = "schemaRegistry";

    public  long registerSchema(Schema schema) throws IOException {
        long fingerprint = getSchemaFingerprint(schema);
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
        List<String> lines = Files.readAllLines(files[0].toPath());
        Iterator<String> it = lines.iterator();
        String content = "";
        while (it.hasNext()) {
            content += it.next();
        }
        Schema schema = new Schema.Parser().parse(content);
        return schema;

    };
}
