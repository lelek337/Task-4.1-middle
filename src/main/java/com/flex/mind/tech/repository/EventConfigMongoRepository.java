package com.flex.mind.tech.repository;

import com.flex.mind.tech.model.entity.EventConfigMongo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.storage.type", havingValue = "mongodb")
public interface EventConfigMongoRepository extends MongoRepository<EventConfigMongo, String> {

    boolean existsByEventTypeAndSource(String eventType, String source);

    @Query("{'$and': [" +
            "{'eventType': {$regex: ?0, $options: 'i'}}, " +
            "{'source': {$regex: ?1, $options: 'i'}}, " +
            "{'enabled': ?2}" +
            "]}")
    Page<EventConfigMongo> findByFilter(String eventType, String source, Boolean enabled, Pageable pageable);
}
