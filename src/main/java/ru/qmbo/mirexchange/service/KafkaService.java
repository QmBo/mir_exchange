package ru.qmbo.mirexchange.service;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class KafkaService {
    @Value("${kafka.topic}")
    private String topic;
    private final KafkaTemplate<Integer, Message> template;

    /**
     * Send message.
     *
     * @param message the message
     */
    public void sendMessage(Message message) {
        this.template.send(topic, message);
    }
}
