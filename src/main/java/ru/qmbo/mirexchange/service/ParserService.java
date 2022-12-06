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

@Service
@Log4j2
public class ParserService {
    @Value("${mir.page}")
    private String page;

    private final RateService rateService;

    public ParserService(RateService rateService) {
        this.rateService = rateService;
    }

    @Scheduled(cron = "0 * * * * ?")
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
