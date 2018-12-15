package com.example.mongosearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;

@SpringBootApplication
@Slf4j
public class MongoSearchApplication {

    /**
     * usage: monogHost totalCount collectionName batchSize generateJsonFlag
     *
     * args[0] = host
     * args[1] = totalCount
     * args[2] = collectionName
     * args[3] = batchSize
     * args[4] = generateJson (true|false)
     */
    @Bean
    public CommandLineRunner run() throws Exception {

        return args -> {

            long starTime = System.currentTimeMillis();

            String mongoHost = args[0];
            int totalCount = Integer.parseInt(args[1]);
            String collectionName = args[2];
            int batchSize = Integer.parseInt(args[3]);
            boolean generateJson = "true".equals(args.length >= 5 ? args[4] : "false");

            File baseDir = null;
            if (generateJson) {
                baseDir = new File("data-" + collectionName);
                if (!baseDir.exists()) {
                    baseDir.mkdir();
                }
            }

            // mongo setup
            MongoService service = new MongoService(mongoHost, "search");
            service.openCollection(collectionName);

            // setup the indexes for this client
            CreateJsonService createJsonService = new CreateJsonService(totalCount);
            service.createIndexes(createJsonService.getAttributeKeys());

            ObjectMapper mapper = new ObjectMapper();
            for (int x = 0; x < totalCount; x = x + batchSize) {

                int thisBatchSize = Math.min(batchSize, (totalCount-x));
                ArrayList<SearchMetadata> list = new ArrayList<>();

                for (int i = 0; i < thisBatchSize; i++) {

                    SearchMetadata searchMetadata = createJsonService.generateRandomMetadata(collectionName);
                    list.add(searchMetadata);

                    // write some json files to disk
                    if (generateJson) {
                        File file = new File(baseDir, searchMetadata.getDocumentId() + ".json");
                        mapper.writeValue(file, searchMetadata);
                    }
                }

                service.addBatch(list);

                log.info(String.format("Added objects to MongoDB: Total=%d, BatchSize=%s",
                        x + thisBatchSize,
                        thisBatchSize));
           }

            log.info("Added " + totalCount + " documents in " + (System.currentTimeMillis()-starTime)/1000 + " sec(s)");
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(MongoSearchApplication.class, args);
    }
}
