package ru.qmbo.mirexchange.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.qmbo.mirexchange.model.Rate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest
class ParserServiceTest {
    @Autowired
    private ParserService parserService;
    @MockBean
    private RateService rateService;
    @Captor
    private ArgumentCaptor<Rate> captor;

    @Test
    public void whenTryToParsThenGetRate() {
        long startTest = System.currentTimeMillis();
        parserService.getPage();
        verify(rateService).newRate(captor.capture());
        assertThat(captor.getValue().getAmount()).isGreaterThan(0F);
        assertThat(captor.getValue().getName()).isEqualTo("Казахстанский тенге");
        assertThat(captor.getValue().getDate().getTime()).isBetween(startTest, System.currentTimeMillis());
    }

}