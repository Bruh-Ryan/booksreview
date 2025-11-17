package com.ryanmadeit.booksreview;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Component
public class BookEventConsumer {
    
    @KafkaListener(
        topics = "book-events",
        groupId = "book-analytics-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeBookEvent(
        @Header("kafka_receivedMessageKey") String key,
        @Payload String payload
    ) {
        System.out.println("=== Received Book Event ===");
        System.out.println("Key: " + key);
        System.out.println("Payload: " + payload);
        System.out.println("===========================");
        
        // TODO: Process the event
        // - Store in analytics database
        // - Update trending books cache
        // - Trigger notifications
        // - Feed recommendation engine
    }
}
