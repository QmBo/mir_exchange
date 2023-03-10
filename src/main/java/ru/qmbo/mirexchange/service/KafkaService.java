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

    private final String topic;
    private final KafkaTemplate<Integer, Message> template;

    /**
     * Instantiates a new Kafka service.
     *
     * @param topic kafka topic
     * @param template the template
     */
    public KafkaService(@Value("${kafka.topic}")String topic, KafkaTemplate<Integer, Message> template) {
        this.topic = topic;
        this.template = template;
    }

    /**
     * Send message.
     *
     * @param topic   the topic
     * @param message the message
     */
    public void sendMessage(Message message) {
        this.template.send(topic, message);
    }
}
