import io.searchbox.client.JestClient;
import net.cloudburo.avro.elasticsearch.ElasticSearchMapper;
import net.cloudburo.elasticsearch.JestDemoApplication;
import org.apache.avro.Schema;
import java.io.*;




public class HelloWorld {

    public static void main(String[] args) throws IOException {
        Schema schema = new Schema.Parser().parse(new File("src/main/avro/helloWorld.avsc"));
        byte[] msg = Sender.sendUserMessage(schema);
        String payloadJson = Receiver.receive(msg);
        System.out.println("Payload: "+payloadJson);

        String mappingJson = ElasticSearchMapper.convertToESMapping(schema);
        System.out.println("ES Mapping: "+ mappingJson);
        
        JestClient cli = JestDemoApplication.jestClient();
        System.out.println("Got Connection "+cli.toString());
        JestDemoApplication.createIndex(cli,"employee","profile", mappingJson);
        //JestDemoApplication.createDoc(cli,payloadJson);
    }
}
