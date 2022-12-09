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
    private final String chatId;
    private final KafkaTemplate<Integer, Message> template;

    /**
     * Instantiates a new Kafka service.
     *
     * @param chatId   the chatId
     * @param template the template
     */
    public KafkaService(@Value("${telegram.chat-id}") String chatId,
                        KafkaTemplate<Integer, Message> template) {
        this.chatId = chatId;
        this.template = template;
    }

    /**
     * Send message.
     *
     * @param topic   the topic
     * @param message the message
     */
    public void sendMessage(String topic, String message) {
        this.template.send(topic, new Message().setMessage(message).setChatId(Long.parseLong(this.chatId)));
    }
}
