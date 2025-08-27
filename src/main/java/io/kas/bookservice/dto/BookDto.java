package io.kas.bookservice.dto;

import io.kas.bookservice.model.Category;
import io.kas.bookservice.model.Language;
import io.kas.bookservice.model.Status;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


public record BookDto(
    UUID id,

    @NotBlank(message = "Title is required")
    String title,

    @NotNull
    @Size(min = 1, message = "At least must bo one author")
    List<String> authors,

    String description,

    @URL(message = "Cover Image should be valid url")
    String coverImage,

    @NotNull(message = "Category is required")
    Category category,

    String publisher,

    @Min(0) @Max(2100)
    Integer publishedYear,

    @NotNull(message = "Language is required")
    Language language,

    @Positive
    Integer pageCount,

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    BigDecimal price,

    @Min(0) @Max(100)
    Integer discount,

    Status status
) {}
