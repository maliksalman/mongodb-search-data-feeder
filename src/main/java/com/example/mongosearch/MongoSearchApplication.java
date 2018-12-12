package com.example.mongosearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

@SpringBootApplication
@Slf4j
public class MongoSearchApplication {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    /**
     * usage: hostAndPort totalCount indexName batchSize
     *
     * args[0] = host
     * args[1] = totalCount
     * args[2] = indexName
     * args[3] = batchSize
     * args[4] = generateJson (true|false)
     *
     * @param restTemplate
     * @return
     * @throws Exception
     */
    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) throws Exception {

        return args -> {

            String mongoHost = args[0];
            String indexName = args[2];
            CreateJsonService createJsonService = new CreateJsonService();

            int totalCount = Integer.parseInt(args[1]);
            int batchSize = Integer.parseInt(args[3]);
            boolean generateJson = "true".equals(args.length >= 5 ? args[4] : "false");

            long starTime = System.currentTimeMillis();

            File baseDir = null;
            if (generateJson) {
                baseDir = new File("data-" + indexName);
                if (!baseDir.exists()) {
                    baseDir.mkdir();
                }
            }

            // all mongo setup
            MongoClient client = new MongoClient(mongoHost);
            MongoDatabase database = client.getDatabase("search");
            database.createCollection(indexName);
            MongoCollection<BsonDocument> collection = database.getCollection(indexName, BsonDocument.class);

            // setup the indexes for this client
            createOneIndex(collection, createJsonService.getAttributeKeys());
//            createInidividualIndexes(collection, createJsonService.getAttributeKeys());

            ObjectMapper mapper = new ObjectMapper();
            for (int x = 0; x < totalCount; x = x + batchSize) {

                ArrayList<BsonDocument> batchDocuments = new ArrayList<>();
                int thisBatchSize = Math.min(batchSize, (totalCount-x));

                for (int i = 0; i < thisBatchSize; i++) {

                    SearchMetadata searchMetadata = createJsonService.generateRandomMetadata(indexName);
                    BsonDocument document = new BsonDocument();
                    document.put("_id", new BsonString(searchMetadata.getDocumentId()));
                    document.put("addDate", new BsonDateTime(searchMetadata.getAddDate().getTime()));
                    for (String key: searchMetadata.getAttributes().keySet()) {
                        document.put(key, new BsonString(searchMetadata.getAttributes().get(key)));
                    }

                    batchDocuments.add(document);

                    // write some json files to disk
                    if (generateJson) {
                        File file = new File(baseDir, searchMetadata.getDocumentId() + ".json");
                        mapper.writeValue(file, searchMetadata);
                    }
                }

                // call to mongo to bulk-insert
                collection.insertMany(batchDocuments);

                log.info(String.format("Added objects to MongoDB: Total=%d, BatchSize=%s",
                        x + thisBatchSize,
                        thisBatchSize));
           }

            log.info("Added " + totalCount + " documents in " + (System.currentTimeMillis()-starTime)/1000 + " sec(s)");
        };
    }

    public void createOneIndex(MongoCollection<BsonDocument> collection, Set<String> keys) {
        BsonDocument keyIndexes = new BsonDocument();
        keyIndexes.put("addDate", new BsonInt32(1));
        for(String key : keys) {
            keyIndexes.put(key, new BsonInt32(1));
        }
        collection.createIndex(keyIndexes);
    }

    public void createInidividualIndexes(MongoCollection<BsonDocument> collection, Set<String> keys) {

        BsonDocument dateIndex = new BsonDocument("addDate", new BsonInt32(1));
        collection.createIndex(dateIndex);

        for(String key : keys) {
            BsonDocument keyIndex = new BsonDocument(key, new BsonInt32(1));
            collection.createIndex(keyIndex);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(MongoSearchApplication.class, args);
    }
}
