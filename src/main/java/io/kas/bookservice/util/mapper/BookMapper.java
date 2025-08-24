package io.kas.bookservice.util.mapper;

import io.kas.bookservice.dto.BookDto;
import io.kas.bookservice.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BookMapper {
  BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);

  BookDto toDto(Book book);
  Book toEntity(BookDto bookDto);
}
