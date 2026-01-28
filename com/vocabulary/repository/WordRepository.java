package com.vocabulary.repository;

import com.vocabulary.entity.WordEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface WordRepository extends MongoRepository<WordEntity, String> {
    // Optional: Check if word already exists to avoid duplicates
    Optional<WordEntity> findByWordIgnoreCase(String word);
    boolean existsByWordIgnoreCase(String word);
}