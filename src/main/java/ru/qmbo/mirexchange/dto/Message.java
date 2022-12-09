package ru.qmbo.mirexchange.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Message
 *
 * @author Victor Egorov (qrioflat@gmail.com).
 * @version 0.1
 * @since 08.12.2022
 */
@Data
@Accessors(chain = true)
public class Message {
    private Long chatId;
    private String message;
}
