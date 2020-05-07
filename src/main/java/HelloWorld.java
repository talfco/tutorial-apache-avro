import org.apache.avro.Schema;
import java.io.*;


public class HelloWorld {

    public static void main(String[] args) throws IOException {
        Schema schema = new Schema.Parser().parse(new File("src/main/avro/helloWorld.avsc"));
        byte[] msg = Sender.sendUserMessage(schema);
        Receiver.receive(msg);
    }
}
