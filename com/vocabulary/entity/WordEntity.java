package com.vocabulary.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "searched_words")
public class WordEntity {

    @Id
    private String id;
    
    @Indexed(unique = true)
    private String word;
    private String language;
    private String meaning;
    private String explanation;
    private List<Example> examples;
    private int count = 0;

    public WordEntity() {}

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<Example> getExamples() { return examples; }
    public void setExamples(List<Example> examples) { this.examples = examples; }
    
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public static class Example {
        private String marathi;
        private String english;

        public Example() {}

        public Example(String marathi, String english) {
            this.marathi = marathi;
            this.english = english;
        }

        public String getMarathi() { return marathi; }
        public void setMarathi(String marathi) { this.marathi = marathi; }

        public String getEnglish() { return english; }
        public void setEnglish(String english) { this.english = english; }
    }
}
