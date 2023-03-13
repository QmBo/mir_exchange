package ru.qmbo.mirexchange.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.qmbo.mirexchange.dto.Message;
import ru.qmbo.mirexchange.model.Rate;
import ru.qmbo.mirexchange.repository.RateRepository;

import java.util.Optional;

import static java.lang.Math.abs;
import static java.lang.String.format;
import static ru.qmbo.mirexchange.service.UserService.RUB;
import static ru.qmbo.mirexchange.service.UserService.TENGE;

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

    private final RateRepository repository;
    private final KafkaService kafkaService;
    private final UserService userService;

    /**
     * Instantiates a new Rate service.
     *
     * @param repository   the repository
     * @param kafkaService the kafka service
     * @param userService  the user service
     */
    public RateService(RateRepository repository, KafkaService kafkaService, UserService userService) {
        this.repository = repository;
        this.kafkaService = kafkaService;
        this.userService = userService;
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
        message = this.addUsuallyToMessage(message, rub);
        log.info(message);
        String finalMessage = message;
        this.userService.findAllSubscribeUsers().forEach(
                user -> this.kafkaService.sendMessage(new Message().setMessage(finalMessage).setChatId(user.getChatId()))
        );
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
        message = this.addUsuallyToMessage(message, rubRate);
        log.info(message);
        String finalMessage = message;
        this.userService.findAllSubscribeUsers().forEach(
                user -> this.kafkaService.sendMessage(new Message().setMessage(finalMessage).setChatId(user.getChatId()))
        );
    }

    private String addUsuallyToMessage(String message, double rubRate) {
        return new StringBuilder().append(message)
                .append("\n1000 тен. = ").append(format("%.0f руб.", 1000 / rubRate))
                .append("\n2000 тен. = ").append(format("%.0f руб.", 2000 / rubRate))
                .append("\n3000 тен. = ").append(format("%.0f руб.", 3000 / rubRate))
                .append("\n4000 тен. = ").append(format("%.0f руб.", 4000 / rubRate))
                .append("\n5000 тен. = ").append(format("%.0f руб.", 5000 / rubRate))
                .append("\n6000 тен. = ").append(format("%.0f руб.", 6000 / rubRate))
                .append("\n7000 тен. = ").append(format("%.0f руб.", 7000 / rubRate))
                .append("\n8000 тен. = ").append(format("%.0f руб.", 8000 / rubRate))
                .append("\n9000 тен. = ").append(format("%.0f руб.", 9000 / rubRate))
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
     * @param chatId   the chat id
     * @param amount   the amount
     * @param currency input currency Tenge or Rub
     * @return the string
     */
    public String calculateRate(String chatId, String amount, String currency) {
        String[] result = {"Wrong Parameters"};
        try {
            int parseAmount = Integer.parseInt(amount);
            long parseChatId = Long.parseLong(chatId);
            this.repository.findTop1ByOrderByDateDesc()
                    .ifPresent(
                            rate -> result[0] = this.sendCalculateMessage(parseChatId, parseAmount,
                                    rate.getAmount(), currency)
                    )
            ;
            this.userService.userCollect(parseChatId);
        } catch (Exception e) {
            log.warn("Parse input value error: {}", e.getMessage());
        }
        return result[0];
    }

    private String sendCalculateMessage(long chatId, int amount, float rate, String inputValue) {
        String result = "";
        if (TENGE.equalsIgnoreCase(inputValue)) {
            result = sendCalculateMessageInputTenge(chatId, amount, amount * rate);
        } else if (RUB.equalsIgnoreCase(inputValue)) {
            result = sendCalculateMessageInputRub(chatId, amount, amount / rate);
        }
        return result;
    }

    private String sendCalculateMessageInputTenge(long chatId, int requestInt, float calculateRate) {
        String message = format("Сегодня %,d тен. = %,.2f руб.", requestInt, calculateRate);
        this.kafkaService.sendMessage(new Message().setMessage(message).setChatId(chatId));
        return message;
    }

    private String sendCalculateMessageInputRub(long chatId, int requestInt, float calculateRate) {
        String message = format("Сегодня %,d руб. = %,.2f тен.", requestInt, calculateRate);
        this.kafkaService.sendMessage(new Message().setMessage(message).setChatId(chatId));
        return message;
    }

    /**
     * Resend message.
     *
     * @return answer message
     */
    public String resend() {
        this.repository.findTop1ByOrderByDateDesc().ifPresent(this::firstRateRecord);
        return "try to resend";
    }
}
