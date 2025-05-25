package org.example.url_shorter_test_task_gorokhov.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlMapping, String> {
    Optional<UrlMapping> findByFullUrl(String url);

}