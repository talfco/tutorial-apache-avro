package net.cloudburo.avro.elasticsearch;

import net.cloudburo.elasticsearch.JestDemoApplication;
import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import static org.apache.avro.Schema.Type.*;

public class ElasticSearchMapper {
    public static final String INDEXER_ATTR = "index_ipid";

    private static Logger logger = Logger.getLogger(ElasticSearchMapper.class);

    public static String convertToESMapping(String index, Schema avroSchema) throws IOException {
        final List<Schema.Field> fields = avroSchema.getFields();
        XContentBuilder builder = XContentFactory.jsonBuilder();

        builder.startObject();
        {


                builder.startObject(index);
                {
                    builder.field("dynamic", "true");
                    builder.field("numeric_detection", true);

                    for (Schema.Field field : fields) {
                        if (field.name().equals(INDEXER_ATTR)) {
                            builder.startObject("_id");
                            builder.field("path",INDEXER_ATTR);
                            builder.endObject();
                        }
                    }

                    builder.startObject("properties");
                    {
                        for (Schema.Field field : fields) {
                            if (field.name().equals(INDEXER_ATTR)) continue;
                            String lookupType = field.schema().getType().getName();
                            if (field.schema().getType() == UNION && field.schema().getTypes().size() > 1) {
                                lookupType = field.schema().getTypes().get(0).getType().getName();
                            }
                            final String normalizeDataType = normalizeDataTypeToES(lookupType);
                            if (normalizeDataType == null) {
                                System.out.println(
                                        "could not handling this type of field - " + field + ", lookupType - "
                                                + lookupType);
                                continue;
                            }

                            builder.startObject(field.name());
                            {
                                builder.field("type", normalizeDataType);
                                if (normalizeDataType.equals("keyword")) {
                                    //for performance tuning
                                    builder.field("ignore_above", 256);
                                    builder.field("norms", false);
                                    builder.field("index_options", "freqs");
                                }
                            }
                            builder.endObject();
                        }
                    }
                    builder.endObject();
                }
                builder.endObject();
        }
        builder.endObject();
        builder.close();
        String mappingJson = Strings.toString(builder);
        logger.debug("ES Mapping JSON: "+mappingJson);
        return  mappingJson;
    }

    private static String normalizeDataTypeToES(String lookupType) {
        CharSequence seq;
        if (lookupType.compareToIgnoreCase(INT.name()) == 0) {
            lookupType = "integer";
        } else if (lookupType.compareToIgnoreCase(STRING.name()) == 0) {
            lookupType = "keyword";
        }

        if (!StringUtils.containsAny(lookupType.toLowerCase()
                , "integer"
                , "long"
                , "double"
                , "float"
                , "boolean"
                , "keyword")) {
            return null;
        }
        return lookupType;
    }
}
