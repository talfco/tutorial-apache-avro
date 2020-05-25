import business.model.strategy.BusinessModelStrategy;
import business.model.strategy.MarketRegionsEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestLoader {

    // The testloader uses the Avro generated Java Binding Classes
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
