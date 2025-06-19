package ru.qmbo.mirexchange.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

@Configuration
public class FormatterConfig {

    @Bean("decimalFormat")
    public DecimalFormat decimalFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        return new DecimalFormat("###,###,###,###,###", symbols);
    }

    @Bean("decimalFormatFloat")
    public DecimalFormat decimalFormatFloat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        return new DecimalFormat("###,###,###,###,###.00", symbols);
    }
}
