package io.kas.bookservice.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import io.kas.bookservice.repository.BookDao;
import org.bson.UuidRepresentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

  @Bean
  public MongoClient mongoClient() {
    ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");

    MongoClientSettings settings = MongoClientSettings.builder()
        .applyConnectionString(connectionString)
        .uuidRepresentation(UuidRepresentation.STANDARD)
        .build();

    return MongoClients.create(settings);
  }

  @Bean
  public BookDao bookDao(MongoClient client) {
    return new BookDao(client);
  }
}
