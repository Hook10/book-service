package io.kas.bookservice.util.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.kas.bookservice.dto.events.BookEvent;
import org.apache.kafka.common.serialization.Serializer;

public class BookEventSerializer implements Serializer<BookEvent> {
  private final ObjectMapper objectMapper;

  public BookEventSerializer() {
    this.objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Override
  public byte[] serialize(String topic, BookEvent data) {
    try {
      return objectMapper.writeValueAsBytes(data);
    } catch (Exception e) {
      throw new RuntimeException("Error serializing BookEvent", e);
    }
  }
}


