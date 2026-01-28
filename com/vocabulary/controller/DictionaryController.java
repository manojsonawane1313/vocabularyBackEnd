package com.vocabulary.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.vocabulary.dto.WordResponse;
import com.vocabulary.service.DeepSeekService;



@RestController
@RequestMapping("/api/dictionary")
@CrossOrigin(origins = "*")
public class DictionaryController {

    private final DeepSeekService deepSeekService; // Updated name

    public DictionaryController(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    @GetMapping("/lookup")
    public ResponseEntity<WordResponse> lookup(@RequestParam String word) {
        return ResponseEntity.ok(deepSeekService.getWordData(word));
    }
}