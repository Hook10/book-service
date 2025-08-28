package io.kas.bookservice.service;

import io.kas.bookservice.dto.BookDto;
import io.kas.bookservice.dto.events.BookEvent;
import io.kas.bookservice.exception.BookNotFoundException;
import io.kas.bookservice.model.Book;
import io.kas.bookservice.repository.BookDao;
import io.kas.bookservice.util.mapper.BookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import com.mongodb.reactivestreams.client.ClientSession;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BookService {

  private final BookDao bookDao;
  private final BookMapper bookMapper;
  private final KafkaSender<String, BookEvent> kafkaSender;

  public Flux<BookDto> getBooks() {
    return bookDao.findAll().map(bookMapper::toDto);
  }

  public Mono<BookDto> getBook(UUID id) {
    return bookDao.findById(id).switchIfEmpty(
            Mono.error(new BookNotFoundException("Book not found with id: " + id)))
        .map(bookMapper::toDto);
  }

  public Mono<BookDto> saveBook(Mono<BookDto> bookDtoMono) {
    return bookDtoMono.map(bookMapper::toEntity)
        .flatMap(entity -> {
          if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
          }
          return executeInTransaction(session ->
              bookDao.saveWithSession(entity, session)
                  .flatMap(savedBook ->
                      sendBookEventWithTransaction(savedBook, "BOOK_CREATED", session)
                          .thenReturn(savedBook)
                  )
          );
        })
        .map(bookMapper::toDto);
  }

  public Mono<BookDto> updateBook(Mono<BookDto> bookDtoMono, UUID id) {
    return bookDao.findById(id)
        .switchIfEmpty(Mono.error(new BookNotFoundException("Book not found with id: " + id)))
        .flatMap(existing ->
            bookDtoMono.map(bookMapper::toEntity)
                .doOnNext(e -> e.setId(id))
                .flatMap(updated ->
                    executeInTransaction(session ->
                        bookDao.updateWithSession(id, existing.getVersion(), updated, session)
                            .flatMap(updatedBook ->
                                sendBookEventWithTransaction(updatedBook, "BOOK_UPDATED", session)
                                    .thenReturn(updatedBook)
                            )
                    )
                )
        )
        .map(bookMapper::toDto);
  }

  public Mono<Void> deleteBook(UUID id) {
    return bookDao.findById(id)
        .switchIfEmpty(Mono.error(new BookNotFoundException("Book not found with id: " + id)))
        .flatMap(existing ->
            executeInTransaction(session ->
                bookDao.deleteWithSession(id, session)
                    .flatMap(deleted -> {
                      if (!deleted) {
                        return Mono.error(new RuntimeException("Failed to delete book " + id));
                      }
                      return sendBookEventWithTransaction(existing, "BOOK_DELETED", session);
                    })
            )
        );
  }

  private <T> Mono<T> executeInTransaction(TransactionalOperation<T> operation) {
    return bookDao.startSession()
        .flatMap(session -> {
          // Start transaction
          session.startTransaction();

          return operation.execute(session)
              .flatMap(result ->
                  // Commit transaction if successful
                  Mono.from(session.commitTransaction())
                      .thenReturn(result)
              )
              .onErrorResume(throwable ->
                  // Rollback transaction on error
                  Mono.from(session.abortTransaction())
                      .then(Mono.error(new RuntimeException("Transaction failed: " + throwable.getMessage(), throwable)))
              )
              .doFinally(signal -> {
                // Always close the session
                session.close();
              });
        });
  }

  private Mono<Void> sendBookEventWithTransaction(Book book, String eventType, ClientSession session) {
    BookEvent event = buildBookEvent(book, eventType);

    return kafkaSender.send(Mono.just(
            SenderRecord.create(
                "book-topic",
                null,
                null,
                event.getPayload().getBookId().toString(),
                event,
                null
            )
        ))
        .timeout(Duration.ofSeconds(10))
        .onErrorResume(ex ->
            // This will trigger transaction rollback
            Mono.error(new RuntimeException("Kafka send failed: " + ex.getMessage(), ex))
        )
        .then();
  }

  private BookEvent buildBookEvent(Book book, String eventType) {
    BookEvent event = new BookEvent();
    event.setEventId(UUID.randomUUID());
    event.setEventType(eventType);
    event.setOccurredAt(Instant.now());

    BookEvent.Payload payload = new BookEvent.Payload();
    payload.setBookId(book.getId());
    payload.setTitle(book.getTitle());
    payload.setAuthors(book.getAuthors());
    payload.setDescription(book.getDescription());
    payload.setCoverImage(book.getCoverImage());
    payload.setCategory(book.getCategory() != null ? book.getCategory().toString() : null);
    payload.setPublisher(book.getPublisher());
    payload.setPublishedYear(book.getPublishedYear());
    payload.setLanguage(book.getLanguage() != null ? book.getLanguage().toString() : null);
    payload.setPageCount(book.getPageCount());
    payload.setPrice(book.getPrice());
    payload.setDiscount(book.getDiscount());
    payload.setStatus(book.getStatus() != null ? book.getStatus().toString() : null);
    payload.setTimestamp(Instant.now());

    event.setPayload(payload);

    return event;
  }

  // Functional interface for transactional operations
  @FunctionalInterface
  private interface TransactionalOperation<T> {
    Mono<T> execute(ClientSession session);
  }
}