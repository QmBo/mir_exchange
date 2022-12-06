package ru.qmbo.mirexchange.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.qmbo.mirexchange.model.Rate;
import ru.qmbo.mirexchange.repository.RateRepository;

import java.util.List;
import java.util.Optional;

import static java.lang.Math.abs;
import static java.lang.String.format;

@Service
@Log4j2
public class RateService {
    private final RateRepository repository;
    private final KafkaService kafkaService;

    public RateService(RateRepository repository, KafkaService kafkaService) {
        this.repository = repository;
        this.kafkaService = kafkaService;
    }

    public void newRate(Rate tenge) {
        Optional<Rate> lastRecord = this.repository.findTop1ByOrderByDateDesc();
        if (lastRecord.isPresent()) {
            boolean rec = false;
            Rate rate = lastRecord.get();
            if (rate.getAmount().compareTo(tenge.getAmount()) != 0) {
                rec = true;
            }
            if (rec) {
                log.info("Exchange rate chang. New rate = {}", tenge.getAmount());
                this.repository.save(tenge);
                this.rateChanged();
            }
        }
    }

    private void rateChanged() {
        List<Rate> lastRates = this.repository.findTop2ByOrderByDateDesc();
        if (lastRates.size() == 2) {
            Float actual = lastRates.get(0).getAmount();
            float div = actual - lastRates.get(1).getAmount();
            float abs = abs(div);
            String firstString = (div < 0.0)
                    ? format("%s %.5f", "Рубль дорожает разница:", abs)
                    : format("%s %.5f", "Рубль дешевеет разница:", abs);
            double rubRate = 1 / actual;
            String secondString = String.format("За рубль сейчас дают %.4f тенге.", rubRate);
            String message = format("%s\n%s", firstString, secondString);
            log.info(firstString);
            log.info(secondString);
            this.kafkaService.sendMessage(message);
        }
    }

}
