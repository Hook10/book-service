package io.kas.bookservice.config;

import io.kas.bookservice.dto.events.BookEvent;
import io.kas.bookservice.util.serializer.BookEventSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

  @Bean
  public KafkaSender<String, BookEvent> kafkaSender() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BookEventSerializer.class);

    SenderOptions<String, BookEvent> senderOptions = SenderOptions.create(props);
    return KafkaSender.create(senderOptions);
  }
}
