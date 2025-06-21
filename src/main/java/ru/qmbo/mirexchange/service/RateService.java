package ru.qmbo.mirexchange.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.qmbo.mirexchange.dto.Message;
import ru.qmbo.mirexchange.model.Rate;
import ru.qmbo.mirexchange.repository.RateRepository;

import java.text.DecimalFormat;
import java.util.Optional;

import static java.lang.Math.abs;
import static java.lang.String.format;
import static ru.qmbo.mirexchange.service.UserService.RUB;
import static ru.qmbo.mirexchange.service.UserService.TENGE;

/**
 * RateService
 *
 * @author Victor Egorov.
 * @version 0.2
 * @since 20.06.2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class RateService {

    private final DecimalFormat decimalFormat;
    private final DecimalFormat decimalFormatFloat;
    private final RateRepository repository;
    private final KafkaService kafkaService;
    private final UserService userService;

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
        StringBuilder sb = new StringBuilder(message);

        for (int i = 1; i <= 10; i++) {
            int tenge = i * 1000;
            String tengeStr = decimalFormat.format(tenge);
            String rubStr = decimalFormat.format(tenge / rubRate);
            sb.append(format(i > 9 ? "\n%s тен. = %s руб." : "\n %s тен. = %s руб.", tengeStr, rubStr));
        }

        return sb.toString();
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
        String message = format("Сегодня по курсу НБК %s тен. = %s руб.",
                decimalFormat.format(requestInt), decimalFormatFloat.format(calculateRate));
        this.kafkaService.sendMessage(new Message().setMessage(message).setChatId(chatId));
        return message;
    }

    private String sendCalculateMessageInputRub(long chatId, int requestInt, float calculateRate) {
        String message = format("Сегодня по курсу НБК %s руб. = %s тен.",
                decimalFormat.format(requestInt), decimalFormatFloat.format(calculateRate));
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
