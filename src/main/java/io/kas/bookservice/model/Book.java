package io.kas.bookservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "books")
public class Book {
  @Id
  private UUID id;

  private String title;

  private List<String> authors;

  private String description;

  private String coverImage;

  private Category category;

  private String publisher;

  private Integer publishedYear;

  private Language language = Language.RU;

  private Integer pageCount;

  private List<PromoInfo> promos;

  @Field(targetType = FieldType.DECIMAL128)
  private BigDecimal price;

  private Integer discount;

  private Status status;

  @Getter
  @Setter(AccessLevel.NONE)
  @Version
  private Long version;


}
