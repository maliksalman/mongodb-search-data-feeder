package com.example.mongosearch;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDateTime;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MongoService {

	private String mongoHost;
	private String dbName;
	private MongoCollection<BsonDocument> collection;

	public MongoService(String mongoHost, String dbName) {
		this.mongoHost = mongoHost;
		this.dbName = dbName;
	}

	public void openCollection(String collectionName) {
		MongoClient client = new MongoClient(mongoHost);
		MongoDatabase database = client.getDatabase(dbName);
		database.createCollection(collectionName);
		collection = database.getCollection(collectionName, BsonDocument.class);
	}

	public void createIndexes(Set<String> keys) {
//		createIndexesWithDate(keys);
//		createOneIndex(keys);
		createInidividualIndexes(keys);
	}

	private void createOneIndex(Set<String> keys) {
		BsonDocument keyIndexes = new BsonDocument();
		keyIndexes.put("date", new BsonInt32(1));
		for(String key : keys) {
			keyIndexes.put(key, new BsonInt32(1));
		}
		collection.createIndex(keyIndexes);
	}

	private void createInidividualIndexes(Set<String> keys) {
		BsonDocument dateIndex = new BsonDocument("date", new BsonInt32(1));
		collection.createIndex(dateIndex);

		for(String key : keys) {
			BsonDocument keyIndex = new BsonDocument(key, new BsonInt32(1));
			collection.createIndex(keyIndex);
		}
	}

	private void createIndexesWithDate(Set<String> keys) {
		for(String key : keys) {
			BsonDocument index = new BsonDocument();
			index.append("date", new BsonInt32(1));
			index.append(key, new BsonInt32(1));
			collection.createIndex(index);
		}
	}

	public void addBatch(List<SearchMetadata> list) {
		ArrayList<BsonDocument> batchDocuments = new ArrayList<>();
		for (SearchMetadata searchMetadata: list) {

			BsonDocument document = new BsonDocument();
			document.put("_id", new BsonString(searchMetadata.getDocumentId()));
			document.put("date", new BsonDateTime(searchMetadata.getDate().getTime()));
			document.put("totalPages", new BsonInt32(searchMetadata.getTotalPages()));

			for (String key: searchMetadata.getAttributes().keySet()) {
				document.put(key, new BsonString(searchMetadata.getAttributes().get(key)));
			}

			batchDocuments.add(document);
		}
		collection.insertMany(batchDocuments);
	}
}
