package io.kas.bookservice.service;

import io.kas.bookservice.dto.BookDto;
import io.kas.bookservice.dto.events.BookEvent;
import io.kas.bookservice.exception.BookNotFoundException;
import io.kas.bookservice.model.Book;
import io.kas.bookservice.repository.BookDao;
import io.kas.bookservice.util.mapper.BookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BookService {

  private final BookDao bookDao;
  private final BookMapper bookMapper;
  private final KafkaTemplate<String, BookEvent> kafkaTemplate;

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
          return bookDao.save(entity)
              .thenReturn(entity)
              .doOnSuccess(saved -> sendBookEvent(saved, "BOOK_CREATED"));
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
                    bookDao.update(id, existing.getVersion(), updated)
                        .thenReturn(updated)
                        .doOnSuccess(u -> sendBookEvent(u, "BOOK_UPDATED"))
                )
        )
        .map(bookMapper::toDto);
  }


  public Mono<Void> deleteBook(UUID id) {
    return bookDao.findById(id)
        .switchIfEmpty(Mono.error(new BookNotFoundException("Book not found with id: " + id)))
        .flatMap(existing -> bookDao.delete(id)
            .then(Mono.fromRunnable(() -> sendBookEvent(existing, "BOOK_DELETED"))));
  }

  private void sendBookEvent(Book book, String eventType) {
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

    kafkaTemplate.send("book-topic", book.getId().toString(), event);
  }

}
