package ru.qmbo.mirexchange.service;

import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.qmbo.mirexchange.model.Rate;

import java.io.IOException;
import java.util.Date;

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
    @Scheduled(fixedDelay = 60000)
    public void getPage() {
        try {
            this.parsPage(Jsoup.connect(this.page).get());
        } catch (IOException e) {
            log.error("Error loading page: {}", e.getMessage());
        }
    }

    private void parsPage(Document document) {
        Elements tr = document.body().getElementsByTag("tr");
        String text = tr.stream().filter(element -> element.text().contains("Казах")).findFirst().orElseThrow().text();
        float amount = Float.parseFloat(
                text.replace("Казахстанский тенге ", "")
                        .replace(",", ".")
        );
        this.rateService.newRate(
                new Rate()
                        .setAmount(amount)
                        .setDate(new Date(System.currentTimeMillis()))
                        .setName("Казахстанский тенге")
        );
    }
}
