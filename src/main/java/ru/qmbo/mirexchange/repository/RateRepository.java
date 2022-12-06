package ru.qmbo.mirexchange.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.qmbo.mirexchange.model.Rate;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface RateRepository extends MongoRepository<Rate, Date> {
    Optional<Rate> findTop1ByOrderByDateDesc();
    List<Rate> findTop2ByOrderByDateDesc();
}
