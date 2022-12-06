package ru.qmbo.mirexchange.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {
    private final String topic;
    private final KafkaTemplate<Integer, String> template;

    public KafkaService(@Value("${telegram.chat-id}") String topic, KafkaTemplate<Integer, String> template) {
        this.topic = topic;
        this.template = template;
    }

    public void sendMessage(String message) {
        this.template.send(this.topic, message);
    }
}
