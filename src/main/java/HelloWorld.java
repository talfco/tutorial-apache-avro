import example.avro.User;
import org.apache.avro.Schema;
import org.apache.avro.SchemaNormalization;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.*;
import java.nio.ByteBuffer;

public class HelloWorld {

    public static void main(String[] args) throws IOException {
        User user1 = new User();
        user1.setName("Alyssa");
        user1.setFavoriteNumber(256);
        // Leave favorite color null

        // Altere constructor
        User user2 = new User(null,"Ben", 7, "red");

        // Construct via builder
        User user3 = User.newBuilder()
                .setAvroFingerprint(null)
                .setName("Charlie")
                .setFavoriteColor("blue")
                .setFavoriteNumber(null)
                .build();

        // Serialize user1, user2 and user3 to disk
        File file =  new File("users.avro");
        DatumWriter<User> userDatumWriter = new SpecificDatumWriter<User>(User.class);
        DataFileWriter<User> dataFileWriter = new DataFileWriter<User>(userDatumWriter);
        dataFileWriter.create(user1.getSchema(), file);
        dataFileWriter.append(user1);
        dataFileWriter.append(user2);
        dataFileWriter.append(user3);
        dataFileWriter.close();

        // Deserialize Users from disk
        DatumReader<User> userDatumReader = new SpecificDatumReader<User>(User.class);
        DataFileReader<User> dataFileReader = new DataFileReader<User>(file, userDatumReader);
        User user = null;
        while (dataFileReader.hasNext()) {
        // Reuse user object by passing it to next(). This saves us from
        // allocating and garbage collecting many objects for files with
        // many items.
            user = dataFileReader.next(user);
            System.out.println(user);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Schema schema = new Schema.Parser().parse(new File("src/main/avro/helloWorld.avsc"));
        long fingerprint = SchemaNormalization.parsingFingerprint64(schema);
        String fiName = Long.valueOf(fingerprint).toString()+"."+schema.getNamespace()+"."+schema.getName()+".avsc";
        PrintWriter out = new PrintWriter("schemaRegistry/"+fiName);
        out.println(schema.toString(true));
        out.close();
        byte[] msg = produceBinaryAvroMessage(user1,schema, fingerprint);
        convertAvroFromBinaryToJSON(msg, schema);
    }

    static ByteBuffer generateAvroSingleObjectEncodingHeader(Schema schema, long fingerprint) {
        byte[] avroMarker = new byte[] { (byte)0xc0, 0x01 };
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES+2);
        buffer.put(avroMarker);
        buffer.putLong(fingerprint);
        return buffer;
    }

    /**
     *
     * @param record An Avro Java Data Object
     * @param schema The Avro Schema associate with the Avro Java OBject
     * @return
     * @throws IOException
     */
    public static byte[] produceBinaryAvroMessage(User record, Schema schema, long fingerprint) throws IOException {
        record.setAvroFingerprint(fingerprint);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //baos.write(createAvroSingleObjectEncodingHeader(schema).array());
        BinaryEncoder encoder = EncoderFactory.get().directBinaryEncoder(baos, null);
        GenericDatumWriter<Object> writer = new GenericDatumWriter<>(schema);
        writer.write(record,encoder);
        encoder.flush();
        return baos.toByteArray();
    }

    static void convertAvroFromBinaryToJSON(byte[] msg, Schema schema)
            throws IOException {

        //FileOutputStream outputStream = new FileOutputStream(new File("users.json"));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        DatumReader<Object> reader = new GenericDatumReader<>(schema);
        DatumWriter<Object> writer = new GenericDatumWriter<>(schema);

        BinaryDecoder binaryDecoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(msg), null);

        JsonEncoder jsonEncoder = EncoderFactory.get().jsonEncoder(schema, outputStream, true);
        Object datum = null;
        while (!binaryDecoder.isEnd()) {
            datum = reader.read(datum, binaryDecoder);
            writer.write(datum, jsonEncoder);
            jsonEncoder.flush();
        }
        outputStream.flush();
        System.out.println(outputStream.toString());
    }



}
