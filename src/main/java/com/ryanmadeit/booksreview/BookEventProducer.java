package com.ryanmadeit.booksreview;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class BookEventProducer {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String TOPIC = "book-events";
    
    public void publishSearchEvent(BookSearchEvent event) {
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(TOPIC, "search-" + event.getSearchType(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Published search event: " + event);
            } else {
                System.err.println("Failed to publish search event: " + ex.getMessage());
            }
        });
    }
    
    public void publishViewEvent(BookViewEvent event) {
        // Use bookId as key for partitioning - same book always goes to same partition
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(TOPIC, event.getBookId(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Published view event: " + event);
            } else {
                System.err.println("Failed to publish view event: " + ex.getMessage());
            }
        });
    }
}
