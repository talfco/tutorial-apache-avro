import io.searchbox.client.JestClient;
import net.cloudburo.avro.elasticsearch.ElasticSearchMapper;
import net.cloudburo.elasticsearch.JestDemoApplication;
import org.apache.avro.Schema;
import java.io.*;
import org.apache.log4j.Logger;

/**
 * The example outlines  the "creation" of a new Data Object.
 * So the Avro Message will consist of exactly 1 record and not an array ob records
 */
public class HelloWorld {

    private static Logger logger = Logger.getLogger(HelloWorld.class);

    public static void main(String[] args) throws IOException {
        Schema schema = new Schema.Parser().parse(new File("src/main/avro/helloWorld.avsc"));
        byte[] msg = Sender.sendUserMessage(schema);
        String payloadJson = Receiver.receive(msg);
        logger.info("Payload JSON: "+payloadJson);

        String mappingJson = ElasticSearchMapper.convertToESMapping("employee","profile",schema);
        logger.info("ES Mapping JSON"+mappingJson);
        JestClient cli = JestDemoApplication.jestClient(args[0], args[1],args[2]);
        System.out.println("Got ElasticSearch Connection "+cli.toString());
        JestDemoApplication.createIndex(cli,"employee","profile", mappingJson);
        JestDemoApplication.createDoc(cli,payloadJson);
    }
}
