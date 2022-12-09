package ru.qmbo.mirexchange.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.qmbo.mirexchange.model.Rate;
import ru.qmbo.mirexchange.repository.RateRepository;

import java.util.Optional;

import static java.lang.Math.abs;
import static java.lang.String.format;

/**
 * RateService
 *
 * @author Victor Egorov (qrioflat@gmail.com).
 * @version 0.1
 * @since 08.12.2022
 */
@Service
@Log4j2
public class RateService {
    private final String topic;
    private final RateRepository repository;
    private final KafkaService kafkaService;

    /**
     * Instantiates a new Rate service.
     *
     * @param topic        the topic
     * @param repository   the repository
     * @param kafkaService the kafka service
     */
    public RateService(@Value("${kafka.topic}")String topic, RateRepository repository, KafkaService kafkaService) {
        this.topic = topic;
        this.repository = repository;
        this.kafkaService = kafkaService;
    }

    /**
     * New rate.
     *
     * @param newRate the new rate
     */
    public void newRate(Rate newRate) {
        Optional<Rate> lastRecord = this.repository.findTop1ByOrderByDateDesc();
        if (lastRecord.isPresent()) {
            boolean rec = false;
            Rate lastRate = lastRecord.get();
            if (lastRate.getAmount().compareTo(newRate.getAmount()) != 0) {
                rec = true;
            }
            if (rec) {
                log.info("Exchange rate chang. New rate = {}", newRate.getAmount());
                this.repository.save(newRate);
                this.rateChanged(newRate, lastRate);
            }
        } else {
            log.info("No exchange rate in data base. New rate = {}", newRate.getAmount());
            this.repository.save(newRate);
            this.firstRateRecord(newRate);
        }
    }

    private void firstRateRecord(Rate rate) {
        float rub = 1 / rate.getAmount();
        String message =
                format("Курс на сегодня: %.4f\nСтатистики курса нет, так как нет более ранней информации о курсе.", rub);
        log.info(message);
        this.kafkaService.sendMessage(this.topic, message);
    }

    private void rateChanged(Rate newRate, Rate lastRate) {
        Float actual = newRate.getAmount();
        float div = actual - lastRate.getAmount();
        float abs = abs(div);
        String firstString = (div < 0.0)
                ? format("%s %.5f", "Рубль дорожает разница:", abs)
                : format("%s %.5f", "Рубль дешевеет разница:", abs);
        double rubRate = 1 / actual;
        String secondString = String.format("За рубль сейчас дают %.4f тенге.", rubRate);
        String message = format("%s\n%s", firstString, secondString);
        log.info(firstString);
        log.info(secondString);
        this.kafkaService.sendMessage(this.topic, message);
    }

    /**
     * Gets last rate.
     *
     * @return the last rate
     */
    public String getLastRate() {
        Float amount = this.repository.findTop1ByOrderByDateDesc().orElse(new Rate().setAmount(0F)).getAmount();
        return String.format(
                "Last amount = %f %s", amount, amount == 0F ? "" : format("=> Now 1 Rub = %.4f Ten.", (1 / amount))
        );
    }
}
