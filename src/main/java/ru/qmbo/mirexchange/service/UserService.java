package ru.qmbo.mirexchange.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
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
public class UserService {
    private final UserRepository repository;

    /**
     * Instantiates a new User service.
     *
     * @param repository the repository
     */
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

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
        AtomicReference<String> result = new AtomicReference<>("Bad Request!");
        try {
            final long parseLong = Long.parseLong(chatId);
            this.repository.findByChatId(parseLong).ifPresentOrElse(
                    user -> result.set(format("User %s already added!", chatId)),
                    () -> result.set(format("%s added!", this.saveUser(parseLong)))
            );
        } catch (NumberFormatException e) {
            log.warn("Bad chat Id: {}", chatId);
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
        AtomicReference<String> result = new AtomicReference<>("Bad Request!");
        try {
            final long parseLong = Long.parseLong(chatId);
            this.repository.findByChatId(parseLong).ifPresentOrElse(
                    user -> result.set(format("%s delete!", this.deleteUser(user))),
                    () -> result.set(format("User %s not found!", chatId))
            );
        } catch (NumberFormatException e) {
            log.warn("Bad chat Id: {}", chatId);
        }
        return result.get();
    }

    private String deleteUser(User user) {
        log.info("User unsubscribe at tenge =(");
        this.repository.delete(user);
        return user.toString();
    }

    private String saveUser(Long parseLong) {
        log.info("New user subscribe at tenge =)");
        final User user = new User().setChatId(parseLong).setName("http").setSubscribe("tenge");
        this.repository.save(user);
        return user.toString();
    }
}
