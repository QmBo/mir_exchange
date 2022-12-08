package ru.qmbo.mirexchange.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.qmbo.mirexchange.model.Rate;
import ru.qmbo.mirexchange.repository.RateRepository;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class RateServiceTest {
    @Autowired
    private RateService rateService;
    @MockBean
    private KafkaService kafkaService;
    @MockBean
    private RateRepository repository;
    @Captor
    private ArgumentCaptor<Rate> rateArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Test
    public void whenNewRateThenWrite() {
        when(repository.findTop1ByOrderByDateDesc()).thenReturn(Optional.empty());
        rateService.newRate(new Rate().setAmount(0.1356F).setName("Каз тен").setDate(new Date()));
        verify(repository).save(rateArgumentCaptor.capture());
        assertThat(rateArgumentCaptor.getAllValues().size()).isEqualTo(1);
        assertThat(rateArgumentCaptor.getValue().getAmount()).isEqualTo(0.1356F);
        assertThat(rateArgumentCaptor.getValue().getName()).isEqualTo("Каз тен");
    }

    @Test
    public void whenNewRateThenWriteAndSendToKafka() {
        when(repository.findTop1ByOrderByDateDesc()).thenReturn(Optional.empty());
        rateService.newRate(new Rate().setAmount(0.1356F).setName("Каз тен").setDate(new Date()));
        verify(kafkaService).sendMessage(anyString(), stringArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue())
                .isEqualTo("Курс на сегодня: 7,3746\nСтатистики курса нет, так ка нет более ранней информации о курсе.");
    }

    @Test
    public void whenNewRateLowAndRateChangThenWriteAndSendToKafka() {
        when(repository.findTop1ByOrderByDateDesc()).thenReturn(Optional.of(new Rate().setAmount(0.1345F)));
        rateService.newRate(new Rate().setAmount(0.1356F).setName("Каз тен").setDate(new Date()));
        verify(kafkaService).sendMessage(anyString(), stringArgumentCaptor.capture());
        verify(repository).save(rateArgumentCaptor.capture());
        assertThat(rateArgumentCaptor.getAllValues().size()).isEqualTo(1);
        assertThat(rateArgumentCaptor.getValue().getAmount()).isEqualTo(0.1356F);
        assertThat(rateArgumentCaptor.getValue().getName()).isEqualTo("Каз тен");
        assertThat(stringArgumentCaptor.getValue())
                .isEqualTo("Рубль дешевеет разница: 0,00110\nЗа рубль сейчас дают 7,3746 тенге.");
    }

    @Test
    public void whenNewRateHiAndRateChangThenWriteAndSendToKafka() {
        when(repository.findTop1ByOrderByDateDesc()).thenReturn(Optional.of(new Rate().setAmount(0.1365F)));
        rateService.newRate(new Rate().setAmount(0.1356F).setName("Каз тен").setDate(new Date()));
        verify(kafkaService).sendMessage(anyString(), stringArgumentCaptor.capture());
        verify(repository).save(rateArgumentCaptor.capture());
        assertThat(rateArgumentCaptor.getAllValues().size()).isEqualTo(1);
        assertThat(rateArgumentCaptor.getValue().getAmount()).isEqualTo(0.1356F);
        assertThat(rateArgumentCaptor.getValue().getName()).isEqualTo("Каз тен");
        assertThat(stringArgumentCaptor.getValue())
                .isEqualTo("Рубль дорожает разница: 0,00090\nЗа рубль сейчас дают 7,3746 тенге.");
    }
}