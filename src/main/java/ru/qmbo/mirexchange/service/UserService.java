package ru.qmbo.mirexchange.service;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.qmbo.mirexchange.model.User;
import ru.qmbo.mirexchange.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

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
    public static final String TENGE = "tenge";
    public static final String RUB = "rub";
    private final UserRepository repository;

    /**
     * Find all users.
     *
     * @return the list of users
     */
    public List<User> findAllSubscribeUsers() {
        return this.repository.findAll()
                .stream()
                .filter(user -> user.getSubscribe() != null)
                .collect(Collectors.toList());
    }
}
