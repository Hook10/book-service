package io.kas.bookservice.repository;

import io.kas.bookservice.model.Book;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookRepository extends ReactiveMongoRepository<Book, UUID> {
}
