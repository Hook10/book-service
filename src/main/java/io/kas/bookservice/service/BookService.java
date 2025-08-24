package io.kas.bookservice.service;

import io.kas.bookservice.dto.BookDto;
import io.kas.bookservice.exception.BookNotFoundException;
import io.kas.bookservice.repository.BookRepository;
import io.kas.bookservice.util.mapper.BookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BookService {

  private final BookRepository bookRepository;
  private final BookMapper bookMapper;

  public Flux<BookDto> getBooks() {
    return bookRepository.findAll().map(bookMapper::toDto);
  }

  public Mono<BookDto> getBook(UUID id) {
    return bookRepository.findById(id).switchIfEmpty(
            Mono.error(new BookNotFoundException("Book not found with id: " + id)))
        .map(bookMapper::toDto);
  }

  public Mono<BookDto> saveBook(Mono<BookDto> bookDtoMono) {
    return bookDtoMono.map(bookMapper::toEntity)
        .flatMap(bookRepository::insert)
        .map(bookMapper::toDto);
  }

  public Mono<BookDto> updateBook(Mono<BookDto> bookDtoMono, UUID id) {
    return bookRepository.findById(id)
        .switchIfEmpty(Mono.error(new BookNotFoundException("Book not found with id: " + id)))
        .flatMap(b -> bookDtoMono.map(bookMapper::toEntity)
            .doOnNext(e -> e.setId(id)))
        .flatMap(bookRepository::save)
        .map(bookMapper::toDto);
  }

  public Mono<Void> deleteBook(UUID id) {
    return bookRepository.findById(id)
        .switchIfEmpty(Mono.error(new BookNotFoundException("Book not found with id: " + id)))
        .flatMap(bookRepository::delete);

  }
}
