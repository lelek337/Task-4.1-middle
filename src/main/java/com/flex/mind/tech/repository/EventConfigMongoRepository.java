package com.flex.mind.tech.repository;

import com.flex.mind.tech.model.entity.EventConfigMongo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ConditionalOnProperty(name = "app.storage.type", havingValue = "mongodb")
public interface EventConfigMongoRepository extends MongoRepository<EventConfigMongo, String> {

    boolean existsByEventTypeAndSource(String eventType, String source);

    List<EventConfigMongo> findByEventTypeAndSourceAndEnabled(String eventType, String source, Boolean enabled);

    List<EventConfigMongo> findByEventTypeAndSource(String eventType, String source);

    List<EventConfigMongo> findByEventTypeAndEnabled(String eventType, Boolean enabled);

    List<EventConfigMongo> findBySourceAndEnabled(String source, Boolean enabled);

    List<EventConfigMongo> findByEventType(String eventType);
    List<EventConfigMongo> findBySource(String source);
    List<EventConfigMongo> findByEnabled(Boolean enabled);

    List<EventConfigMongo> findAll();
}
