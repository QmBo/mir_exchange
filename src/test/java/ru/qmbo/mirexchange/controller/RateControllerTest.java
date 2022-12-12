package ru.qmbo.mirexchange.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import ru.qmbo.mirexchange.dto.Message;
import ru.qmbo.mirexchange.model.Rate;
import ru.qmbo.mirexchange.repository.RateRepository;
import ru.qmbo.mirexchange.service.KafkaService;

@SpringBootTest
@AutoConfigureMockMvc
public class RateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KafkaService kafkaService;
    @MockBean
    private RateRepository rateRepository;
    
    @Captor
    private ArgumentCaptor<Message> captor;

    @Test
    public void whenCalculateRateThenMessageToKafka() throws Exception {
        when(rateRepository.findTop1ByOrderByDateDesc()).thenReturn(Optional.of(new Rate().setAmount(0.13456F)));
        mockMvc.perform(MockMvcRequestBuilders.get("/calc?amount=10000000&chatId=345678"))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isOk());
        verify(kafkaService).sendMessage(anyString(), captor.capture());
        assertThat(captor.getValue().getMessage()).isEqualTo("Сегодня 10 000 000 тен. = 1 345 600,00 руб.");
    }
}
