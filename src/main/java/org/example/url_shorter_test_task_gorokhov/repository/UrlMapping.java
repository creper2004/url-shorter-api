package org.example.url_shorter_test_task_gorokhov.repository;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "url_mapping", indexes = {
        @Index(name = "idx_url_short_code", columnList = "shortCode")
})
public class UrlMapping {
    @Id
    private String shortCode;

    @Column(nullable = false, unique = true, length = 2048)
    private String fullUrl;

    @Column(nullable = false)
    private Instant updatedAt;
}