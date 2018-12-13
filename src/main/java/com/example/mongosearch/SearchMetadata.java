package com.example.mongosearch;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class SearchMetadata {
    private String indexId;
    private String documentId;
    private Date date;
    private Map<String, String> attributes;
    private int totalPages;
}
