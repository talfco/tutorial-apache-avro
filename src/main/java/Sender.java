
import net.cloudburo.elasticsearch.ESPersistencyManager;
import java.io.IOException;

public class Sender {

    private static Sender sender;
    private ESPersistencyManager esManager;

    public static   Sender createSingleton(String esURI, String user, String password, String esIndex) throws IOException{
        if (sender == null)
            sender = new Sender(esURI,user,password,esIndex);
        return sender;
    }

    protected Sender(String esURI, String user, String password, String esIndex) throws IOException{
        esManager = ESPersistencyManager.createSingleton(esURI, user,password);
        if (!esManager.existsIndex(esIndex)) {
            esManager.createIndex(esIndex);
        }
    }



}
