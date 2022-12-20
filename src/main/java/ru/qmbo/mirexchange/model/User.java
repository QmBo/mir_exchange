package ru.qmbo.mirexchange.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Rate
 *
 * @author Victor Egorov (qrioflat@gmail.com).
 * @version 0.1
 * @since 19.12.2022
 */
@Accessors(chain = true)
@Data
@Document
public class User {
    @Id
    private Long chatId;
    private String name;
    private String subscribe;
}
