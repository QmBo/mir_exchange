package ru.qmbo.mirexchange.service;

import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.qmbo.mirexchange.model.Rate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * ParserService
 *
 * @author Victor Egorov (qrioflat@gmail.com).
 * @version 0.1
 * @since 08.12.2022
 */
@Service
@Log4j2
public class ParserService {
    @Value("${mir.page}")
    private String page;

    private final RateService rateService;

    /**
     * Instantiates a new Parser service.
     *
     * @param rateService the rate service
     */
    public ParserService(RateService rateService) {
        this.rateService = rateService;
    }

    /**
     * Gets page.
     */
//    @Scheduled(cron = "0 * * * * ?")
    @Scheduled(fixedDelay = 60, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void getPage() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            this.parsPage(
                    Jsoup.connect(format("%s%s", this.page, LocalDateTime.now().format(formatter)))
                            .get()
            );
        } catch (IOException e) {
            log.error("Error loading page: {}", e.getMessage());
        }
    }

    private void parsPage(Document document) {
        Elements item = document.getElementsByTag("item");
        Element rubElement = item.stream()
                .filter(element -> element.text().contains("РОССИЙСКИЙ РУБЛЬ"))
                .findFirst()
                .orElseThrow();

        float amount = 1F / Float.parseFloat(
                rubElement.getElementsByTag("description")
                        .text()
        );
        this.rateService.newRate(
                new Rate()
                        .setAmount(amount)
                        .setDate(new Date(System.currentTimeMillis()))
                        .setName("Казахстанский тенге")
        );
    }
}
