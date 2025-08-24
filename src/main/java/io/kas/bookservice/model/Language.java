package io.kas.bookservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Language {
  RU,
  EN,
  KZ;

  @JsonCreator
  public static Language fromString(String language) {
    return Language.valueOf(language.toUpperCase());
  }

  @JsonValue
  public String toJson() {
    return this.name().toLowerCase();
  }
}
