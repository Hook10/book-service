package io.kas.bookservice.controller;

import io.kas.bookservice.dto.BookDto;
import io.kas.bookservice.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/books")
public class BookController {

  private final BookService bookService;

  @GetMapping
  public Flux<BookDto> getBooks() {
    return bookService.getBooks();
  }

  @GetMapping("{id}")
  public Mono<BookDto> getBookById(@PathVariable UUID id) {
    return bookService.getBook(id);
  }

  @PostMapping
  public Mono<BookDto> createBook(@Valid @RequestBody Mono<BookDto> bookDtoMono) {
    return bookService.saveBook(bookDtoMono);
  }

  @PutMapping("{id}")
  public Mono<BookDto> updateBook(@Valid @RequestBody Mono<BookDto> bookDtoMono, @PathVariable UUID id) {
    return bookService.updateBook(bookDtoMono, id);
  }

  @DeleteMapping("{id}")
  public Mono<Void> deleteBook(@PathVariable UUID id) {
    return bookService.deleteBook(id);
  }
}
