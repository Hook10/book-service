package io.kas.bookservice.repository;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.reactivestreams.client.ClientSession;
import com.mongodb.reactivestreams.client.MongoClient;
import io.kas.bookservice.model.Book;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class BookDao extends BaseDao<Book> {

  public BookDao(MongoClient client) {
    super(client, "book_db", "books", Book.class);
  }

  public Mono<Book> save(Book book) {
    if (book.getVersion() == null) {
      book.setVersion(0L);
    }
    return Mono.from(collection.insertOne(book))
        .then(Mono.just(book));
  }

  // Transactional save method
  public Mono<Book> saveWithSession(Book book, ClientSession session) {
    if (book.getVersion() == null) {
      book.setVersion(0L);
    }
    return Mono.from(collection.insertOne(session, book))
        .then(Mono.just(book));
  }

  public Flux<Book> findAll() {
    return Flux.from(collection.find());
  }

  public Mono<Book> findById(UUID id) {
    Bson filter = Filters.eq("_id", id);
    return Mono.from(collection.find(filter));
  }

  // Transactional find method
  public Mono<Book> findByIdWithSession(UUID id, ClientSession session) {
    Bson filter = Filters.eq("_id", id);
    return Mono.from(collection.find(session, filter));
  }

  public Mono<Book> update(UUID id, long expectedVersion, Book book) {
    Bson filter = Filters.and(
        Filters.eq("_id", id),
        Filters.eq("version", expectedVersion)
    );

    Bson updates = Updates.combine(
        Updates.set("title", book.getTitle()),
        Updates.set("authors", book.getAuthors()),
        Updates.set("description", book.getDescription()),
        Updates.set("coverImage", book.getCoverImage()),
        Updates.set("category", book.getCategory()),
        Updates.set("publisher", book.getPublisher()),
        Updates.set("publishedYear", book.getPublishedYear()),
        Updates.set("language", book.getLanguage()),
        Updates.set("pageCount", book.getPageCount()),
        Updates.set("promos", book.getPromos()),
        Updates.set("price", book.getPrice()),
        Updates.set("discount", book.getDiscount()),
        Updates.set("status", book.getStatus()),
        Updates.set("version", expectedVersion + 1)
    );

    return Mono.from(collection.findOneAndUpdate(filter, updates))
        .flatMap(updatedDoc -> {
          if (updatedDoc == null) {
            return Mono.error(new IllegalStateException("Optimistic lock failed for book " + id));
          }
          return findById(id);
        });
  }

  // Transactional update method
  public Mono<Book> updateWithSession(UUID id, long expectedVersion, Book book, ClientSession session) {
    Bson filter = Filters.and(
        Filters.eq("_id", id),
        Filters.eq("version", expectedVersion)
    );

    Bson updates = Updates.combine(
        Updates.set("title", book.getTitle()),
        Updates.set("authors", book.getAuthors()),
        Updates.set("description", book.getDescription()),
        Updates.set("coverImage", book.getCoverImage()),
        Updates.set("category", book.getCategory()),
        Updates.set("publisher", book.getPublisher()),
        Updates.set("publishedYear", book.getPublishedYear()),
        Updates.set("language", book.getLanguage()),
        Updates.set("pageCount", book.getPageCount()),
        Updates.set("promos", book.getPromos()),
        Updates.set("price", book.getPrice()),
        Updates.set("discount", book.getDiscount()),
        Updates.set("status", book.getStatus()),
        Updates.set("version", expectedVersion + 1)
    );

    return Mono.from(collection.findOneAndUpdate(session, filter, updates))
        .flatMap(updatedDoc -> {
          if (updatedDoc == null) {
            return Mono.error(new IllegalStateException("Optimistic lock failed for book " + id));
          }
          return findByIdWithSession(id, session);
        });
  }

  public Mono<Boolean> delete(UUID id) {
    return Mono.from(collection.deleteOne(eq("_id", id)))
        .map(result -> result.getDeletedCount() == 1)
        .defaultIfEmpty(false);
  }

  // Transactional delete method
  public Mono<Boolean> deleteWithSession(UUID id, ClientSession session) {
    return Mono.from(collection.deleteOne(session, eq("_id", id)))
        .map(result -> result.getDeletedCount() == 1)
        .defaultIfEmpty(false);
  }

  // Helper method to start a session
  public Mono<ClientSession> startSession() {
    return Mono.from(client.startSession());
  }
}