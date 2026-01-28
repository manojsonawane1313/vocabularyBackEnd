package com.vocabulary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.vocabulary.dto.WordResponse;
import com.vocabulary.entity.WordEntity;
import com.vocabulary.repository.WordRepository;
import com.vocabulary.service.DeepSeekService;



@RestController
@RequestMapping("/api/dictionary")
@CrossOrigin(origins = "*")
public class DictionaryController {

    private final DeepSeekService deepSeekService;
    private final WordRepository wordRepository; // Inject the repo

    public DictionaryController(DeepSeekService deepSeekService, WordRepository wordRepository) {
        this.deepSeekService = deepSeekService;
        this.wordRepository = wordRepository;
    }

    @GetMapping("/lookup")
    public ResponseEntity<WordResponse> lookup(@RequestParam String word) {
        return ResponseEntity.ok(deepSeekService.getWordData(word));
    }
    
 // 1. Fetch all words from MongoDB
    @GetMapping("/history")
    public ResponseEntity<List<WordEntity>> getHistory() {
        return ResponseEntity.ok(wordRepository.findAll());
    }

    // 2. Delete a word from MongoDB by ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteWord(@PathVariable String id) {
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