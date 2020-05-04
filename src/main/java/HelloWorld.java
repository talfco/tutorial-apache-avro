import example.avro.User;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.*;

import java.io.*;
import java.nio.ByteBuffer;

public class HelloWorld {

    public static void main(String[] args) throws IOException {
        byte[] msg = Sender.sendUserMessage();
        Receiver.receive(msg);
    }
}
