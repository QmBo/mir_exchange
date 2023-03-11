package ru.qmbo.mirexchange.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.qmbo.mirexchange.dto.Message;
import ru.qmbo.mirexchange.model.Rate;
import ru.qmbo.mirexchange.model.User;
import ru.qmbo.mirexchange.repository.RateRepository;
import ru.qmbo.mirexchange.repository.UserRepository;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.qmbo.mirexchange.service.UserService.TENGE;

@SpringBootTest
@Testcontainers
class RateServiceTest {

    @Container
    public static MongoDBContainer mongoDB = new MongoDBContainer(
            DockerImageName.parse("mongo:4.0.10"));


    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDB::getReplicaSetUrl);
    }

    @Autowired
    private RateService rateService;
    @MockBean
    private KafkaService kafkaService;
    @MockBean
    private RateRepository rateRepository;
    @MockBean
    private UserRepository userRepository;
    @Captor
    private ArgumentCaptor<Rate> rateArgumentCaptor;
    @Captor
    private ArgumentCaptor<Message> messageArgumentCaptor;

    @Test
    public void whenNewRateThenWrite() {
        when(rateRepository.findTop1ByOrderByDateDesc()).thenReturn(Optional.empty());
        rateService.newRate(new Rate().setAmount((float) 0.1356).setName("Каз тен").setDate(new Date()));
        verify(rateRepository).save(rateArgumentCaptor.capture());
        assertThat(rateArgumentCaptor.getAllValues().size()).isEqualTo(1);
        assertThat(rateArgumentCaptor.getValue().getAmount()).isEqualTo(0.1356F);
        assertThat(rateArgumentCaptor.getValue().getName()).isEqualTo("Каз тен");
    }

    @Test
    public void whenNewRateThenWriteAndSendToKafka() {
        when(rateRepository.findTop1ByOrderByDateDesc()).thenReturn(Optional.empty());
        when(userRepository.findAll()).thenReturn(Collections.singletonList(new User().setChatId(111L).setSubscribe(TENGE)));
        rateService.newRate(new Rate().setAmount(0.1356F).setName("Каз тен").setDate(new Date()));
        verify(kafkaService).sendMessage(messageArgumentCaptor.capture());
        assertThat(messageArgumentCaptor.getValue().getMessage())
                .isEqualTo("Курс на сегодня: 7,3746\nСтатистики курса нет, так как нет более ранней информации о курсе.\n1000 тен. = 136 руб.\n2000 тен. = 271 руб.\n3000 тен. = 407 руб.\n4000 тен. = 542 руб.\n5000 тен. = 678 руб.\n6000 тен. = 814 руб.\n7000 тен. = 949 руб.\n8000 тен. = 1085 руб.\n9000 тен. = 1220 руб.");
    }

    @Test
    public void whenNewRateLowAndRateChangThenWriteAndSendToKafka() {
        when(rateRepository.findTop1ByOrderByDateDesc()).thenReturn(Optional.of(new Rate().setAmount(0.1345F)));
        when(userRepository.findAll()).thenReturn(Collections.singletonList(new User().setChatId(111L).setSubscribe(TENGE)));
        rateService.newRate(new Rate().setAmount(0.1356F).setName("Каз тен").setDate(new Date()));
        verify(kafkaService).sendMessage(messageArgumentCaptor.capture());
        verify(rateRepository).save(rateArgumentCaptor.capture());
        assertThat(rateArgumentCaptor.getAllValues().size()).isEqualTo(1);
        assertThat(rateArgumentCaptor.getValue().getAmount()).isEqualTo(0.1356F);
        assertThat(rateArgumentCaptor.getValue().getName()).isEqualTo("Каз тен");
        assertThat(messageArgumentCaptor.getValue().getMessage())
                .isEqualTo("Рубль дешевеет разница: 0,00110\nЗа рубль сейчас дают 7,3746 тенге.\n1000 тен. = 136 руб.\n2000 тен. = 271 руб.\n3000 тен. = 407 руб.\n4000 тен. = 542 руб.\n5000 тен. = 678 руб.\n6000 тен. = 814 руб.\n7000 тен. = 949 руб.\n8000 тен. = 1085 руб.\n9000 тен. = 1220 руб.");
    }

    @Test
    public void whenNewRateHiAndRateChangThenWriteAndSendToKafka() {
        when(rateRepository.findTop1ByOrderByDateDesc()).thenReturn(Optional.of(new Rate().setAmount(0.1365F)));
        when(userRepository.findAll()).thenReturn(Collections.singletonList(new User().setChatId(111L).setSubscribe(TENGE)));
        rateService.newRate(new Rate().setAmount(0.1356F).setName("Каз тен").setDate(new Date()));
        verify(kafkaService).sendMessage(messageArgumentCaptor.capture());
        verify(rateRepository).save(rateArgumentCaptor.capture());
        assertThat(rateArgumentCaptor.getAllValues().size()).isEqualTo(1);
        assertThat(rateArgumentCaptor.getValue().getAmount()).isEqualTo(0.1356F);
        assertThat(rateArgumentCaptor.getValue().getName()).isEqualTo("Каз тен");
        assertThat(messageArgumentCaptor.getValue().getMessage())
                .isEqualTo("Рубль дорожает разница: 0,00090\nЗа рубль сейчас дают 7,3746 тенге.\n1000 тен. = 136 руб.\n2000 тен. = 271 руб.\n3000 тен. = 407 руб.\n4000 тен. = 542 руб.\n5000 тен. = 678 руб.\n6000 тен. = 814 руб.\n7000 тен. = 949 руб.\n8000 тен. = 1085 руб.\n9000 тен. = 1220 руб.");
    }

    @Test
    public void whenCalculateThenReturnMessage() {
        when(rateRepository.findTop1ByOrderByDateDesc()).thenReturn(Optional.of(new Rate().setAmount(0.1365F)));
        String result = rateService.calculateRate("123456", "1000");
        verify(kafkaService).sendMessage(messageArgumentCaptor.capture());
        assertThat(messageArgumentCaptor.getValue().getChatId()).isEqualTo(123456L);
        assertThat(result).isEqualTo("Сегодня 1 000 тен. = 136,50 руб.");
    }
}