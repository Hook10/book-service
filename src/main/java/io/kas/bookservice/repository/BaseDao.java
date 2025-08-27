package io.kas.bookservice.repository;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;

public abstract class BaseDao<T> {

  protected final MongoCollection<T> collection;

  protected BaseDao(MongoClient client, String dbName, String collectionName, Class<T> clazz) {
    this.collection = client
        .getDatabase(dbName)
        .getCollection(collectionName, clazz);
  }

  public MongoCollection<T> getCollection() {
    return collection;
  }
}
