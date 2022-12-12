package ru.qmbo.mirexchange.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.qmbo.mirexchange.dto.Message;

/**
 * KafkaService
 *
 * @author Victor Egorov (qrioflat@gmail.com).
 * @version 0.1
 * @since 08.12.2022
 */
@Service
public class KafkaService {
    private final KafkaTemplate<Integer, Message> template;

    /**
     * Instantiates a new Kafka service.
     *
     * @param template the template
     */
    public KafkaService(KafkaTemplate<Integer, Message> template) {
        this.template = template;
    }

    /**
     * Send message.
     *
     * @param topic   the topic
     * @param message the message
     */
    public void sendMessage(String topic, Message message) {
        this.template.send(topic, message);
    }
}
