package ru.qmbo.mirexchange.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.qmbo.mirexchange.model.User;
import ru.qmbo.mirexchange.repository.UserRepository;

import java.util.List;

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
        String result = "Bad Request!";
        try {
            final long parseLong = Long.parseLong(chatId);
            final User user = new User().setChatId(parseLong).setName("http").setSubscribe("tenge");
            this.repository.save(user);
            result = format("%s added!", user);
        } catch (NumberFormatException e) {
            log.warn("Bad chat Id: {}", chatId);
        }
        return result;
    }
}
