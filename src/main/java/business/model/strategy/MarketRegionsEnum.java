/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package business.model.strategy;
/** Market Regions Enumeration using UNO M49 standard: https://unstats.un.org/unsd/methodology/m49 */
@org.apache.avro.specific.AvroGenerated
public enum MarketRegionsEnum implements org.apache.avro.generic.GenericEnumSymbol<MarketRegionsEnum> {
  M49_150, M49_142, M49_021, M49_756, other  ;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"MarketRegionsEnum\",\"namespace\":\"business.model.strategy\",\"doc\":\"Market Regions Enumeration using UNO M49 standard: https://unstats.un.org/unsd/methodology/m49\",\"symbols\":[\"M49_150\",\"M49_142\",\"M49_021\",\"M49_756\",\"other\"]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
}
