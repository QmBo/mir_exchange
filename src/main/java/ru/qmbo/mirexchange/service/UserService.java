package ru.qmbo.mirexchange.service;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.qmbo.mirexchange.dto.Message;
import ru.qmbo.mirexchange.model.User;
import ru.qmbo.mirexchange.repository.UserRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;

/**
 * RateService
 *
 * @author Victor Egorov (qrioflat@gmail.com).
 * @version 0.1
 * @since 16.12.2022
 */
@Service
@Log4j2
@AllArgsConstructor
public class UserService {
    public static final String YOU_ARE_SUBSCRIBE = "Вы подписались на рассылку!";
    public static final String YOU_ARE_NOT_SUBSCRIBE = "Вы уже подписаны на рассылку!";
    public static final String ADDED = "%s added!";
    public static final String USER_S_ALREADY_ADDED = "User %s already added!";
    public static final String BAD_REQUEST = "Bad Request!";
    public static final String BAD_CHAT_ID = "Bad chat Id: {}";
    public static final String TENGE = "tenge";
    public static final String HTTP = "http";
    public static final String NEW_USER_SUBSCRIBE_AT_TENGE = "New user subscribe at tenge =)";
    public static final String USER_UNSUBSCRIBE_AT_TENGE = "User unsubscribe at tenge =(";
    public static final String S_DELETE = "%s delete!";
    public static final String USER_S_NOT_FOUND = "User %s not found!";
    public static final String YOU_ARE_UNSUBSCRIBE = "Вы отписались от рассылки!";
    public static final String YOU_ARE_NOT_UNSUBSCRIBE = "Вы не были подписаны на рассылку!";
    private final UserRepository repository;

    private final KafkaService kafkaService;

    /**
     * Find all users.
     *
     * @return the list of users
     */
    public List<User> findAllUsers() {
        return this.repository.findAll();
    }

    /**
     * Add user from HTTP.
     *
     * @param chatId the chat id
     * @return result message
     */
    public String addUser(String chatId) {
        AtomicReference<String> result = new AtomicReference<>(BAD_REQUEST);
        try {
            final long parseLong = Long.parseLong(chatId);
            this.repository.findByChatId(parseLong).ifPresentOrElse(
                    user -> {
                        result.set(format(USER_S_ALREADY_ADDED, chatId));
                        kafkaService.sendMessage(new Message().setMessage(YOU_ARE_NOT_SUBSCRIBE).setChatId(parseLong));
                    },
                    () -> {
                        result.set(format(ADDED, this.saveUser(parseLong)));
                        kafkaService.sendMessage(new Message().setMessage(YOU_ARE_SUBSCRIBE).setChatId(parseLong));
                    }
            );
        } catch (NumberFormatException e) {
            log.warn(BAD_CHAT_ID, chatId);
        }
        return result.get();
    }

    /**
     * Delete User from subscribe.
     *
     * @param chatId chat id of User
     * @return result message
     */
    public String dellUser(String chatId) {
        AtomicReference<String> result = new AtomicReference<>(BAD_REQUEST);
        try {
            final long parseLong = Long.parseLong(chatId);
            this.repository.findByChatId(parseLong).ifPresentOrElse(
                    user -> {
                        result.set(format(S_DELETE, this.deleteUser(user)));
                        kafkaService.sendMessage(new Message().setMessage(YOU_ARE_UNSUBSCRIBE).setChatId(parseLong));
                    },
                    () -> {
                        result.set(format(USER_S_NOT_FOUND, chatId));
                        kafkaService.sendMessage(new Message().setMessage(YOU_ARE_NOT_UNSUBSCRIBE).setChatId(parseLong));
                    }
            );
        } catch (NumberFormatException e) {
            log.warn(BAD_CHAT_ID, chatId);
        }
        return result.get();
    }

    private String deleteUser(User user) {
        log.info(USER_UNSUBSCRIBE_AT_TENGE);
        this.repository.delete(user);
        return user.toString();
    }

    private String saveUser(Long parseLong) {
        log.info(NEW_USER_SUBSCRIBE_AT_TENGE);
        final User user = new User().setChatId(parseLong).setName(HTTP).setSubscribe(TENGE);
        this.repository.save(user);
        return user.toString();
    }
}
