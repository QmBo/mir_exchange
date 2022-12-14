package ru.qmbo.mirexchange.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.qmbo.mirexchange.dto.Message;
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
    private final String chatId;
    private final RateRepository repository;
    private final KafkaService kafkaService;

    /**
     * Instantiates a new Rate service.
     *
     * @param topic        the topic
     * @param chatId       the chat id
     * @param repository   the repository
     * @param kafkaService the kafka service
     */
    public RateService(@Value("${kafka.topic}")String topic,
                       @Value("${telegram.chat-id}") String chatId,
                       RateRepository repository, KafkaService kafkaService) {
        this.topic = topic;
        this.chatId = chatId;
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
                format("???????? ???? ??????????????: %.4f\n???????????????????? ?????????? ??????, ?????? ?????? ?????? ?????????? ???????????? ???????????????????? ?? ??????????.", rub);
        message = this.addUsuallyToMessage(message, rub);
        log.info(message);
        this.kafkaService.sendMessage(
                this.topic, new Message().setMessage(message).setChatId(Long.parseLong(this.chatId))
        );
    }

    private void rateChanged(Rate newRate, Rate lastRate) {
        Float actual = newRate.getAmount();
        float div = actual - lastRate.getAmount();
        float abs = abs(div);
        String firstString = (div < 0.0)
                ? format("%s %.5f", "?????????? ???????????????? ??????????????:", abs)
                : format("%s %.5f", "?????????? ???????????????? ??????????????:", abs);
        double rubRate = 1 / actual;
        String secondString = String.format("???? ?????????? ???????????? ???????? %.4f ??????????.", rubRate);
        String message = format("%s\n%s", firstString, secondString);
        message = this.addUsuallyToMessage(message, rubRate);
        log.info(message);
        this.kafkaService.sendMessage(
                this.topic, new Message().setMessage(message).setChatId(Long.parseLong(this.chatId))
        );
    }

    private String addUsuallyToMessage(String message, double rubRate) {
    return new StringBuilder().append(message)
        .append("\n1000 ??????. = ").append(format("%.0f ??????.", 1000 / rubRate))
        .append("\n2000 ??????. = ").append(format("%.0f ??????.", 2000 / rubRate))
        .append("\n3000 ??????. = ").append(format("%.0f ??????.", 3000 / rubRate))
        .append("\n4000 ??????. = ").append(format("%.0f ??????.", 4000 / rubRate))
        .append("\n5000 ??????. = ").append(format("%.0f ??????.", 5000 / rubRate))
        .append("\n6000 ??????. = ").append(format("%.0f ??????.", 6000 / rubRate))
        .append("\n7000 ??????. = ").append(format("%.0f ??????.", 7000 / rubRate))
        .append("\n8000 ??????. = ").append(format("%.0f ??????.", 8000 / rubRate))
        .append("\n9000 ??????. = ").append(format("%.0f ??????.", 9000 / rubRate))
        .toString();
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

    /**
     * Calculate exchange rate.
     *
     * @param chatId the chat id
     * @param amount the amount
     * @return the string
     */
    public String calculateRate(String chatId, String amount) {
        String[] result = {"Wrong Parameters"};
        try {
            int parseAmount = Integer.parseInt(amount);
            long parseChatId = Long.parseLong(chatId);
            this.repository.findTop1ByOrderByDateDesc()
                .ifPresent(
                        rate -> result[0] = this.sendCalculateMessage(parseChatId, parseAmount, parseAmount * rate.getAmount()))
            ;
        } catch (Exception e) {
            log.warn("Parse input value error: {}", e.getMessage());
        }
        return result[0];
	}

    private String sendCalculateMessage(long chatId, int requestInt, float calculateRate) {
        String message = format("?????????????? %,d ??????. = %,.2f ??????.", requestInt, calculateRate);
        this.kafkaService.sendMessage(this.topic, new Message().setMessage(message).setChatId(chatId));
        return message;
    }
}
