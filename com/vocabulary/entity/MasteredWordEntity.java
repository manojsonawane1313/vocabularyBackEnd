package com.vocabulary.entity;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "mastered_words") 
public class MasteredWordEntity extends WordEntity {
}