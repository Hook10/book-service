package io.kas.bookservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Category {
  FICTION,
  SCIENCE,
  EDUCATION,
  CHILDREN,
  BIOGRAPHY,
  BUSINESS,
  PSYCHOLOGY,
  FANTASY,
  HISTORY,
  ART,
  COOKING,
  COMICS,
  TECHNOLOGY,
  TRAVEL,
  HOME,
  HEALTH,
  RELIGION,
  LANGUAGES;

  @JsonCreator
  public static Category fromString(String category) {
    return Category.valueOf(category.toUpperCase());
  }

  @JsonValue
  public String toJson() {
    return this.name().toLowerCase();
  }
}
