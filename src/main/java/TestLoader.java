import business.model.strategy.BusinessModelStrategy;
import business.model.strategy.MarketRegionsEnum;
import example.avro.User;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    public static BusinessModelStrategy getBusinessModelStrategy() {
        // Sales Marketing Regions
        List<MarketRegionsEnum> regions = new ArrayList<>();
        regions.add(MarketRegionsEnum.M49_021);
        regions.add(MarketRegionsEnum.M49_756);
        // Sales Countries
        List<java.lang.CharSequence> countries_021 = new ArrayList<>();
        countries_021.add("840"); // USA
        countries_021.add("021"); // Canada
        return BusinessModelStrategy.newBuilder()
                .setAvroFingerprint(0)
                .setIndexIpid("0")
                .setLastUpdateTimestamp(0)
                .setLastUpdateLoginid("")
                .setPartnerIpid(UUID.randomUUID().toString())
                .setClientStructure("Approaching the high net worth client segment")
                .setDrivers("Fintecs")
                .setSalesRegions(regions)
                .setSalesCountries021(countries_021)
                .build();
    }
}
