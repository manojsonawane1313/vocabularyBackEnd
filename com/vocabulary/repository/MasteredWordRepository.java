package com.vocabulary.repository;

import com.vocabulary.entity.MasteredWordEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MasteredWordRepository extends MongoRepository<MasteredWordEntity, String> {
}