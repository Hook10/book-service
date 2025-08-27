package io.kas.bookservice.model;

import lombok.*;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Book {
  @BsonId
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

  private BigDecimal price;

  private Integer discount;

  private Status status;

  @BsonIgnore
  private Long version;
}
