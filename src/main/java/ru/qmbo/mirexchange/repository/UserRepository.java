package ru.qmbo.mirexchange.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.qmbo.mirexchange.model.User;


/**
 * RateRepository
 *
 * @author Victor Egorov (qrioflat@gmail.com).
 * @version 0.1
 * @since 16.12.2022
 */
public interface UserRepository extends MongoRepository<User, Long> {

}
