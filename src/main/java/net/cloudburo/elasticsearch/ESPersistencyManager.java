package net.cloudburo.elasticsearch;

import com.google.gson.JsonObject;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Get;
import io.searchbox.core.Index;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.IndicesExists;
import io.searchbox.indices.mapping.PutMapping;
import org.apache.log4j.Logger;

import java.io.IOException;

public class ESPersistencyManager {

    private static ESPersistencyManager persistencyManager;
    private static Logger logger = Logger.getLogger(ESPersistencyManager.class);

    private JestClient esClient;

    public static ESPersistencyManager createSingleton(String esURL, String user, String password) {
        if (persistencyManager==null)
            persistencyManager = new ESPersistencyManager(esURL,user,password);
        return persistencyManager;
    }

    protected ESPersistencyManager(String esURL, String user, String password) {
            esClient = connectES(esURL,user,password);
    }

    public void createIndex(String index) throws IOException {
        logger.info("Creating elasticsearch index: "+index);
        JestResult res = esClient.execute(new CreateIndex.Builder(index).build());
        if (!res.isSucceeded()) {
            logger.error(res.getErrorMessage());
            throw new IOException(res.getErrorMessage());
        }

    }

    public boolean existsIndex(String index) throws IOException {
        return esClient.execute(new IndicesExists.Builder(index).build()).isSucceeded();
    }

    public void updateESMapping(String index, String type, String mappingJson) throws IOException {
        JestResult res = esClient.execute(new PutMapping.Builder(index,type, mappingJson).build());
        if (!res.isSucceeded()) {
            logger.error(res.getErrorMessage());
            throw new IOException(res.getErrorMessage());
        }
    }

    public void createUpdateDocument(String index, String type, String docJson, String id) throws IOException {
        Index esIndex;
        if (id == null) {
            esIndex = new Index.Builder(docJson).index(index).type(type).build();
        } else {
            esIndex = new Index.Builder(docJson).index(index).type(type).id(id).build();
        }
        JestResult jestResult =  esClient.execute(esIndex);
        if(jestResult.isSucceeded()) {
            logger.info("Document persisted");
        }
        else {
            logger.error(jestResult);
        }
    }

    public String readDocumentById(String index,String id) throws IOException{
        return esClient.execute(new Get.Builder(index, id).build()).getJsonString();
    }

    public JsonObject readDocumentByIdAsObject(String index, String id) throws IOException{
        return esClient.execute(new Get.Builder(index, id).build()).getJsonObject();
    }

    private  JestClient connectES(String esURL, String user, String password) {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(
                new HttpClientConfig.Builder(esURL)
                        .multiThreaded(true)
                        .defaultMaxTotalConnectionPerRoute(2)
                        .maxTotalConnection(20)
                        .defaultCredentials(user,password)
                        .build());
        return factory.getObject();
    }


}
