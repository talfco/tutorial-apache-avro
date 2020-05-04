import example.avro.User;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class TestLoader {

    private static User user1,user2,user3;

    public static void createAvroFile() throws IOException {

        user1 = User.newBuilder()
                .setAvroFingerprint(0)
                .setIndexIpid(UUID.randomUUID().toString())
                .setLastUpdateTimestamp(UUID.randomUUID().timestamp())
                .setLastUpdateLoginid("12345678")
                .setPartnerIpid(UUID.randomUUID().toString())
                .setName("Charlie")
                .setFavoriteColor(null)
                .setFavoriteNumber(null)
                .build();

        user2 = User.newBuilder()
                .setAvroFingerprint(0)
                .setIndexIpid(UUID.randomUUID().toString())
                .setLastUpdateTimestamp(UUID.randomUUID().timestamp())
                .setLastUpdateLoginid("111114444")
                .setPartnerIpid(UUID.randomUUID().toString())
                .setName("Alice")
                .setFavoriteColor("red")
                .setFavoriteNumber(4)
                .build();

        // Construct via builder
        user3 = User.newBuilder()
                .setAvroFingerprint(0)
                .setIndexIpid(UUID.randomUUID().toString())
                .setLastUpdateTimestamp(UUID.randomUUID().timestamp())
                .setLastUpdateLoginid("33334444")
                .setPartnerIpid(UUID.randomUUID().toString())
                .setName("Bob")
                .setFavoriteColor("green")
                .setFavoriteNumber(null)
                .build();

        // Serialize user1, user2 and user3 to disk
        File file = new File("users.avro");
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
    }

    public static User getUser() {
        return User.newBuilder()
                .setAvroFingerprint(0)
                .setIndexIpid(UUID.randomUUID().toString())
                .setLastUpdateTimestamp( java.lang.System.currentTimeMillis())
                .setLastUpdateLoginid("111114444")
                .setPartnerIpid(UUID.randomUUID().toString())
                .setName("Alice")
                .setFavoriteColor("red")
                .setFavoriteNumber(null)
                .build();
    }
}
