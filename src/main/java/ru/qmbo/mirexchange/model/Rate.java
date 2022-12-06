package ru.qmbo.mirexchange.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Accessors(chain = true)
@Data
@Document
public class Rate {
    @Id
    private Date date;
    private String name;
    private Float amount;
}
