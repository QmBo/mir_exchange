package ru.qmbo.mirexchange.controller;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.qmbo.mirexchange.model.Rate;
import ru.qmbo.mirexchange.repository.RateRepository;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static ru.qmbo.mirexchange.controller.RateController.WRONG_INPUT_VALUE_PARAMETER;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class RateControllerTest {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaGroupId;

    @Value("${kafka.topic}")
    private String kafkaTopic;

    private KafkaConsumer<String, String> consumer;

    private KafkaProducer<String, String> producer;

    @Container
    public static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:latest"));


    @Container
    public static MongoDBContainer mongoDB = new MongoDBContainer(
            DockerImageName.parse("mongo:4.0.10"));


    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDB::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    public void setUp() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        properties.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        properties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, String.class);

        consumer = new KafkaConsumer<>(properties);

        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        producer = new KafkaProducer<>(producerProps);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RateRepository rateRepository;


    @Test
    public void whenCalculateRateThenMessageToKafka() throws Exception {
        when(rateRepository.findTop1ByOrderByDateDesc()).thenReturn(Optional.of(new Rate().setAmount(0.13456F)));
        mockMvc.perform(MockMvcRequestBuilders.get("/calc?amount=10000000&chatId=345678"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
        consumer.subscribe(Collections.singletonList(kafkaTopic));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000L));
        consumer.close();

        List<ConsumerRecord<String, String>> result = new ArrayList<>(100);
        records.forEach(result::add);
        List<String> messages = result.stream().map(ConsumerRecord::value).collect(Collectors.toList());

        assertThat(messages).contains("{\"chatId\":345678,\"message\":\"Сегодня 10 000 000 тен. = 1 345 600,00 руб.\"}");
    }

    @Test
    public void whenCalculateRateWithWrongParameterThenWrongParameterAnswer() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/calc?amount=10000000&chatId=345678&currency=Rur"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo(String.format(WRONG_INPUT_VALUE_PARAMETER, "Rur"));
    }

    @Test
    public void whenCalculateRateWithRubThenMessageToKafka() throws Exception {
        when(rateRepository.findTop1ByOrderByDateDesc()).thenReturn(Optional.of(new Rate().setAmount(0.13456F)));
        mockMvc.perform(MockMvcRequestBuilders.get("/calc?amount=10000&chatId=345678&currency=Rub"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
        consumer.subscribe(Collections.singletonList(kafkaTopic));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000L));
        consumer.close();

        List<ConsumerRecord<String, String>> result = new ArrayList<>(100);
        records.forEach(result::add);
        List<String> messages = result.stream().map(ConsumerRecord::value).collect(Collectors.toList());

        assertThat(messages).contains("{\"chatId\":345678,\"message\":\"Сегодня 10 000 руб. = 74 316,29 тен.\"}");
    }

    @Test
    public void whenCalculateRateWithRub2ThenMessageToKafka() throws Exception {
        when(rateRepository.findTop1ByOrderByDateDesc()).thenReturn(Optional.of(new Rate().setAmount(0.13456F)));
        mockMvc.perform(MockMvcRequestBuilders.get("/calc?chatId=303775921&amount=100&currency=rub"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
        consumer.subscribe(Collections.singletonList(kafkaTopic));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000L));
        consumer.close();

        List<ConsumerRecord<String, String>> result = new ArrayList<>(100);
        records.forEach(result::add);
        List<String> messages = result.stream().map(ConsumerRecord::value).collect(Collectors.toList());

        assertThat(messages).contains("{\"chatId\":303775921,\"message\":\"Сегодня 100 руб. = 743,16 тен.\"}");
    }
}
