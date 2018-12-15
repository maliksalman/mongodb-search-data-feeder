package com.example.mongosearch;

import java.util.*;

public class CreateJsonService {

    private static final String[] POSSIBLE_ATTRIBUTE_KEYS = new String[] {
        "accountNumber", "accountType", "residentId", "externalId", "recipientName",
        "cardId", "taxId", "statementId", "residentId", "lastName", "firstName"
    };
    private static final long MILLIS_IN_YEAR = 1000 * 60 * 60 * 24 * 365;
    private static final String[] DOCUMENT_TYPES = { "statement", "eob", "idcard", "summary" };

    private Random random = new Random();
    private Set<String> attributeKeys;
    private String[] possibleValues;

    private GregorianCalendar gregorianCalendar;
    private long startTime;

    public CreateJsonService(int total) {
        this.gregorianCalendar = new GregorianCalendar();
        this.startTime = System.currentTimeMillis();

        // pick the random keys
        this.attributeKeys = new HashSet<>();
        int keysCount = Math.max(POSSIBLE_ATTRIBUTE_KEYS.length/2, random.nextInt(POSSIBLE_ATTRIBUTE_KEYS.length));
        for (int i = 0; i < keysCount; i++) {
            this.attributeKeys.add(POSSIBLE_ATTRIBUTE_KEYS[random.nextInt(POSSIBLE_ATTRIBUTE_KEYS.length)]);
        }

        // pick the possible values
        int totalPossibleCount = total / 12;
        possibleValues = new String[totalPossibleCount];
        for (int i = 0; i < totalPossibleCount; i++) {
            possibleValues[i] = UUID.randomUUID().toString();
        }
    }

    public Set<String> getAttributeKeys() {
        return attributeKeys;
    }

    public SearchMetadata generateRandomMetadata(String indexId) {

        gregorianCalendar.setTimeInMillis(startTime);
        gregorianCalendar.add(Calendar.DAY_OF_YEAR, -random.nextInt(365));
        Date date = gregorianCalendar.getTime();

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
            attributes.put(key, possibleValues[random.nextInt(possibleValues.length)]);
        }

        // common attributes
        attributes.put("documentType", getRandomDocumentType());

        return attributes;
    }

    private String getRandomDocumentType() {
        return DOCUMENT_TYPES[random.nextInt(DOCUMENT_TYPES.length)];
    }
}
