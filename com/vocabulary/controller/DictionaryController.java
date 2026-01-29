package com.vocabulary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.vocabulary.dto.WordResponse;
import com.vocabulary.entity.MasteredWordEntity;
import com.vocabulary.entity.WordEntity;
import com.vocabulary.repository.MasteredWordRepository;
import com.vocabulary.repository.WordRepository;
import com.vocabulary.service.DeepSeekService;



@RestController
@RequestMapping("/api/dictionary")
@CrossOrigin(origins = "*")
public class DictionaryController {

    private final DeepSeekService deepSeekService;
    private final WordRepository wordRepository; // Inject the repo
    private final MasteredWordRepository masteredRepository;

    public DictionaryController(DeepSeekService deepSeekService, WordRepository wordRepository,MasteredWordRepository masteredRepository) {
        this.deepSeekService = deepSeekService;
        this.wordRepository = wordRepository;
        this.masteredRepository = masteredRepository;
    }
    
    @PutMapping("/increment-count/{id}")
    public ResponseEntity<?> incrementCount(@PathVariable String id) {
        return wordRepository.findById(id).map(word -> {
            // 1. Increment logic
            int newCount = word.getCount() + 1;
            word.setCount(newCount);

            if (newCount >= 10) {
                // 1. Create the Mastered version
                MasteredWordEntity mastered = new MasteredWordEntity();
                
                // 2. Copy the data (You can use a library or manual setters)
                mastered.setId(word.getId());
                mastered.setWord(word.getWord());
                mastered.setMeaning(word.getMeaning());
                mastered.setLanguage(word.getLanguage());
                mastered.setExplanation(word.getExplanation());
                mastered.setExamples(word.getExamples());
                mastered.setCount(newCount);

                // 3. Save to 'mastered_words' collection
                masteredRepository.save(mastered); 
                
                // 4. Delete from 'searched_words' collection
                wordRepository.deleteById(id);
                
                return ResponseEntity.ok("{\"status\": \"mastered\"}");
            } else {
                // 4. UPDATE EXISTING
                WordEntity updated = wordRepository.save(word);
                return ResponseEntity.ok(updated);
            }
        }).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/lookup")
    public ResponseEntity<WordResponse> lookup(@RequestParam String word) {
        return ResponseEntity.ok(deepSeekService.getWordData(word));
    }
    
 // 1. Fetch all words from word MongoDB
    @GetMapping("/history")
    public ResponseEntity<List<WordEntity>> getHistory() {
        return ResponseEntity.ok(wordRepository.findAll());
    }
    
    //get words from mastery 
    @GetMapping("/master")
    public ResponseEntity<List<MasteredWordEntity>> getmastery() {
        return ResponseEntity.ok(masteredRepository.findAll());
    }

    // 2. Delete master word
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteWord(@PathVariable String id) {
    	masteredRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    // 2. Delete current word
    @DeleteMapping("/deleteCurrent/{id}")
    public ResponseEntity<Void> deleteCWord(@PathVariable String id) {
    	wordRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    

    @PostMapping("/save")
    public ResponseEntity<?> saveWord(@RequestBody WordResponse response) {
        // 1. Clean the input word (Remove leading/trailing spaces)
        String cleanWord = response.word().trim();

        // 2. Strict Check: If it exists, do not save
        if (wordRepository.existsByWordIgnoreCase(cleanWord)) {
            return ResponseEntity.status(HttpStatus.OK)
                                 .body("{\"message\": \"Word already in history\"}");
        }
        
        

        // 3. Map and Save
        WordEntity entity = new WordEntity();
        entity.setWord(cleanWord); // Save the trimmed version
        entity.setLanguage(response.language());
        entity.setMeaning(response.meaning());
        entity.setExplanation(response.explanation());
        
        List<WordEntity.Example> entityExamples = response.examples().stream()
            .map(ex -> {
                WordEntity.Example e = new WordEntity.Example();
                e.setMarathi(ex.marathi());
                e.setEnglish(ex.english());
                return e;
            }).toList();
        
        entity.setExamples(entityExamples);
        
        return ResponseEntity.ok(wordRepository.save(entity));
    }
}