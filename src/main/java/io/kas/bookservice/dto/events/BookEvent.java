package io.kas.bookservice.dto.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookEvent {
  private UUID eventId;
  private String eventType;
  private Instant occurredAt;
  private Payload payload;

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Payload {
    private UUID bookId;
    private String title;
    private List<String> authors;
    private String description;
    private String coverImage;
    private String category;
    private String publisher;
    private Integer publishedYear;
    private String language;
    private Integer pageCount;
    private BigDecimal price;
    private Integer discount;
    private String status;
    private Instant timestamp;
  }
}

