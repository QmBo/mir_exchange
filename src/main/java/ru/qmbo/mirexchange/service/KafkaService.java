package ru.qmbo.mirexchange.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * KafkaService
 *
 * @author Victor Egorov (qrioflat@gmail.com).
 * @version 0.1
 * @since 08.12.2022
 */
@Service
public class KafkaService {
    private final String prefix;
    private final KafkaTemplate<Integer, String> template;

    /**
     * Instantiates a new Kafka service.
     *
     * @param prefix   the prefix
     * @param template the template
     */
    public KafkaService(@Value("${telegram.chat-id}") String prefix,
                        KafkaTemplate<Integer, String> template) {
        this.prefix = prefix;
        this.template = template;
    }

    /**
     * Send message.
     *
     * @param topic   the topic
     * @param message the message
     */
    public void sendMessage(String topic, String message) {
        this.template.send(topic, String.format("%s_%s", prefix, message));
    }
}
