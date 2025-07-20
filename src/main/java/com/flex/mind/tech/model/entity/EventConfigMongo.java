package com.flex.mind.tech.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "event_configs")
@CompoundIndexes({
        @CompoundIndex(name = "event_type_source_idx", def = "{'eventType': 1, 'source': 1}", unique = true),
        @CompoundIndex(name = "enabled_created_idx", def = "{'enabled': 1, 'createdAt': -1}")
})
public class EventConfigMongo {

    @Id
    private String id;

    @Field("event_type")
    @Indexed
    private String eventType;

    @Field("source")
    @Indexed
    private String source;

    @Field("enabled")
    @Indexed
    private Boolean enabled;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
}
