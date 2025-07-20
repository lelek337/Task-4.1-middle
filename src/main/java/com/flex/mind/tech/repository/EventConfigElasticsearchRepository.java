package com.flex.mind.tech.repository;

import com.flex.mind.tech.model.entity.EventConfigElastic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ConditionalOnProperty(name = "app.storage.type", havingValue = "elasticsearch")
public interface EventConfigElasticsearchRepository extends ElasticsearchRepository<EventConfigElastic, String> {

    boolean existsByEventTypeAndSource(String eventType, String source);

    @Query("{\"bool\":{\"must\":[" +
            "{\"term\":{\"eventType.keyword\":\"?0\"}}," +
            "{\"term\":{\"source.keyword\":\"?1\"}}," +
            "{\"term\":{\"enabled\":?2}}" +
            "]}}")
    List<EventConfigElastic> findByEventTypeAndSourceAndEnabled(String eventType, String source, Boolean enabled);

    @Query("{\"bool\":{\"must\":[" +
            "{\"term\":{\"eventType.keyword\":\"?0\"}}," +
            "{\"term\":{\"source.keyword\":\"?1\"}}" +
            "]}}")
    List<EventConfigElastic> findByEventTypeAndSource(String eventType, String source);

    @Query("{\"bool\":{\"must\":[" +
            "{\"term\":{\"eventType.keyword\":\"?0\"}}," +
            "{\"term\":{\"enabled\":?1}}" +
            "]}}")
    List<EventConfigElastic> findByEventTypeAndEnabled(String eventType, Boolean enabled);

    @Query("{\"bool\":{\"must\":[" +
            "{\"term\":{\"source.keyword\":\"?0\"}}," +
            "{\"term\":{\"enabled\":?1}}" +
            "]}}")
    List<EventConfigElastic> findBySourceAndEnabled(String source, Boolean enabled);

    @Query("{\"term\":{\"eventType.keyword\":\"?0\"}}")
    List<EventConfigElastic> findByEventType(String eventType);

    @Query("{\"term\":{\"source.keyword\":\"?0\"}}")
    List<EventConfigElastic> findBySource(String source);

    @Query("{\"term\":{\"enabled\":?0}}")
    List<EventConfigElastic> findByEnabled(Boolean enabled);

    @Query("{\"match_all\":{}}")
    List<EventConfigElastic> findAll();;
}
