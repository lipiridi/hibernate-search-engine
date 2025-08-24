package io.github.lipiridi.searchengine.repository;

import io.github.lipiridi.searchengine.entity.SimpleEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimpleEntityRepository extends JpaRepository<SimpleEntity, UUID> {}
