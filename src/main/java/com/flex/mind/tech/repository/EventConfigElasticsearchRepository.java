package com.flex.mind.tech.repository;

import com.flex.mind.tech.model.entity.EventConfigElastic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.storage.type", havingValue = "elasticsearch")
public interface EventConfigElasticsearchRepository extends ElasticsearchRepository<EventConfigElastic, String> {

    boolean existsByEventTypeAndSource(String eventType, String source);

    Page<EventConfigElastic> findByEventTypeContainingIgnoreCaseAndSourceContainingIgnoreCaseAndEnabled(
            String eventType, String source, Boolean enabled, Pageable pageable);
}
