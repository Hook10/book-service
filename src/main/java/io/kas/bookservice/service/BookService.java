package io.kas.bookservice.service;

import io.kas.bookservice.dto.BookDto;
import io.kas.bookservice.exception.BookNotFoundException;
import io.kas.bookservice.exception.OptimisticLockingFailureException;
import io.kas.bookservice.model.Book;
import io.kas.bookservice.repository.BookDao;
import io.kas.bookservice.util.mapper.BookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BookService {

  private final BookDao bookDao;
  private final BookMapper bookMapper;

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
          return bookDao.save(entity).thenReturn(entity);
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
                    bookDao.update(id, existing.getVersion(), updated) // returns Mono<Book>
                )
        )
        .map(bookMapper::toDto);
  }


  public Mono<Void> deleteBook(UUID id) {
    return bookDao.findById(id)
        .switchIfEmpty(Mono.error(new BookNotFoundException("Book not found with id: " + id)))
        .flatMap(existing -> bookDao.delete(id))
        .flatMap(success -> {
          if (success) {
            return Mono.empty();
          } else {
            return Mono.error(new RuntimeException("Failed to delete book " + id));
          }
        });
  }
}
