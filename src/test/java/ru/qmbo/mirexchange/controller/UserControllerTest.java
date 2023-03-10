package ru.qmbo.mirexchange.controller;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.qmbo.mirexchange.model.User;
import ru.qmbo.mirexchange.repository.UserRepository;
import ru.qmbo.mirexchange.service.UserService;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class UserControllerTest {


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
    private UserRepository userRepository;

    @Test
    public void whenSubscribeThenMessageToKafka() throws Exception {
        when(userRepository.findByChatId(345678L)).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get("/users/add?chatId=345678"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
        consumer.subscribe(Collections.singletonList(kafkaTopic));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000L));
        consumer.close();
        boolean receive = false;

        for (ConsumerRecord<String, String> record : records) {
            System.out.println("record.value() = " + record.value());
            if (record.value().contains(UserService.YOU_ARE_SUBSCRIBE)) {
                receive = true;
                break;
            }
        }
        assertThat(receive).isTrue();
    }

    @Test
    public void whenSubscribeAndAlreadyExistThenMessageToKafka() throws Exception {
        when(userRepository.findByChatId(345678L)).thenReturn(Optional.of(new User().setChatId(345678L)));
        mockMvc.perform(MockMvcRequestBuilders.get("/users/add?chatId=345678"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
        consumer.subscribe(Collections.singletonList(kafkaTopic));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000L));
        consumer.close();
        boolean receive = false;

        for (ConsumerRecord<String, String> record : records) {
            System.out.println("record.value() = " + record.value());
            if (record.value().contains(UserService.YOU_ARE_NOT_SUBSCRIBE)) {
                receive = true;
                break;
            }
        }
        assertThat(receive).isTrue();
    }

    @Test
    public void whenUnsubscribeThenMessageToKafka() throws Exception {
        when(userRepository.findByChatId(345678L)).thenReturn(Optional.of(new User().setChatId(345678L)));
        mockMvc.perform(MockMvcRequestBuilders.get("/users/dell?chatId=345678"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
        consumer.subscribe(Collections.singletonList(kafkaTopic));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000L));
        consumer.close();
        boolean receive = false;

        for (ConsumerRecord<String, String> record : records) {
            System.out.println("record.value() = " + record.value());
            if (record.value().contains(UserService.YOU_ARE_UNSUBSCRIBE)) {
                receive = true;
                break;
            }
        }
        assertThat(receive).isTrue();
    }


    @Test
    public void whenUnsubscribeButNotFoundThenMessageToKafka() throws Exception {
        when(userRepository.findByChatId(345678L)).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get("/users/dell?chatId=345678"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
        consumer.subscribe(Collections.singletonList(kafkaTopic));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000L));
        consumer.close();
        boolean receive = false;

        for (ConsumerRecord<String, String> record : records) {
            System.out.println("record.value() = " + record.value());
            if (record.value().contains(UserService.YOU_ARE_NOT_UNSUBSCRIBE)) {
                receive = true;
                break;
            }
        }
        assertThat(receive).isTrue();
    }
}