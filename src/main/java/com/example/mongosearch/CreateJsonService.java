package com.example.mongosearch;

import java.util.*;

public class CreateJsonService {

    private static final String[] POSSIBLE_ATTRIBUTE_KEYS = new String[] {
        "accountNumber", "accountType", "residentId", "externalId", "recipientName",
        "cardId", "taxId", "statementId", "residentId", "lastName", "firstName"
    };
    private static final int MILLIS_IN_YEAR = 1000 * 60 * 60 * 24 * 365;
    private static final String[] DOCUMENT_TYPES = { "statement", "eob", "idcard", "summary" };

    private Random random = new Random();
    private Set<String> attributeKeys;

    public CreateJsonService() {
        this.attributeKeys = new HashSet<>();
        int keysCount = Math.max(POSSIBLE_ATTRIBUTE_KEYS.length/2, random.nextInt(POSSIBLE_ATTRIBUTE_KEYS.length));
        for (int i = 0; i < keysCount; i++) {
            this.attributeKeys.add(POSSIBLE_ATTRIBUTE_KEYS[random.nextInt(POSSIBLE_ATTRIBUTE_KEYS.length)]);
        }
    }

    public Set<String> getAttributeKeys() {
        return attributeKeys;
    }

    public SearchMetadata generateRandomMetadata(String indexId) {
        Date date = new Date(System.currentTimeMillis() - random.nextInt(MILLIS_IN_YEAR));
        return  SearchMetadata.builder().documentId(UUID.randomUUID().toString())
                                        .indexId(indexId)
                                        .date(date)
                                        .attributes(randomAttributes())
                                        .totalPages(Math.max(1, random.nextInt(10)))
                                        .build();
    }

    private Map<String, String> randomAttributes() {

        HashMap<String, String> attributes = new HashMap<>();

        // random attributes
        for (String key: attributeKeys) {
            attributes.put(key, UUID.randomUUID().toString());
        }

        // common attributes
        attributes.put("documentType", getRandomDocumentType());

        return attributes;
    }

    private String getRandomDocumentType() {
        return DOCUMENT_TYPES[random.nextInt(DOCUMENT_TYPES.length)];
    }
}
