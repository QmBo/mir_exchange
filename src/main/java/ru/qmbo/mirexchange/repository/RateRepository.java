package ru.qmbo.mirexchange.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.qmbo.mirexchange.model.Rate;

import java.util.Date;
import java.util.Optional;

/**
 * RateRepository
 *
 * @author Victor Egorov (qrioflat@gmail.com).
 * @version 0.1
 * @since 08.12.2022
 */
public interface RateRepository extends MongoRepository<Rate, Date> {
    /**
     * Find top 1 by order by date desc optional.
     *
     * @return the optional
     */
    Optional<Rate> findTop1ByOrderByDateDesc();
}
