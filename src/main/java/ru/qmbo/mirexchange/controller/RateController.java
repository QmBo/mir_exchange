package ru.qmbo.mirexchange.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.qmbo.mirexchange.service.RateService;

import static ru.qmbo.mirexchange.service.UserService.RUB;
import static ru.qmbo.mirexchange.service.UserService.TENGE;

/**
 * RateController
 *
 * @author Victor Egorov (qrioflat@gmail.com).
 * @version 0.1
 * @since 08.12.2022
 */
@Controller
public class RateController {
    public static final String WRONG_INPUT_VALUE_PARAMETER = "Wrong inputValue parameter: %s\n Use only tente or rub parameter";
    private final RateService rateService;

    /**
     * Instantiates a new Rate controller.
     *
     * @param rateService the rate service
     */
    public RateController(RateService rateService) {
        this.rateService = rateService;
    }

    /**
     * Gets last rate.
     *
     * @return the last rate
     */
    @GetMapping
    @ResponseBody
    public String getLastRate() {
        return this.rateService.getLastRate();
    }

    @GetMapping("/calc")
    @ResponseBody
    public String calculateRate(@RequestParam String amount, @RequestParam String chatId,
                                @RequestParam(required = false) String currency) {
        if (currency == null || currency.isEmpty()) {
            currency = TENGE;
        } else if (!(TENGE.equalsIgnoreCase(currency) || RUB.equalsIgnoreCase(currency))) {
            return String.format(WRONG_INPUT_VALUE_PARAMETER, currency);
        }
        return this.rateService.calculateRate(chatId, amount, currency);
    }

    @GetMapping("/resend")
    @ResponseBody
    public String resend() {
        return this.rateService.resend();
    }
}
