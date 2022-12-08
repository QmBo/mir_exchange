package ru.qmbo.mirexchange.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Rate
 *
 * @author Victor Egorov (qrioflat@gmail.com).
 * @version 0.1
 * @since 08.12.2022
 */
@Accessors(chain = true)
@Data
@Document
public class Rate {
    @Id
    private Date date;
    private String name;
    private Float amount;
}
