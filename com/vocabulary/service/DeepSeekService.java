package com.vocabulary.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabulary.dto.WordResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DeepSeekService {

    @Value("${grok.api.url}")
    private String apiUrl;

    @Value("${grok.api.key}")
    private String apiKey;

    @Value("${grok.api.model}") // Read model from properties
    private String apiModel;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public DeepSeekService(RestClient.Builder builder, ObjectMapper objectMapper) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public WordResponse getWordData(String word) {
        String prompt = "You are a dictionary. For the English word '" + word + "', " +
                "provide a STRICT JSON response (no markdown) with fields: " +
                "1. 'meaning' (in Marathi), " +
                "2. 'explanation' (in Marathi), " +
                "3. 'examples' (list of 3 Marathi sentences).";

        // Use the model defined in application.properties
        Map<String, Object> requestBody = Map.of(
            "model", apiModel, 
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            ),
            "response_format", Map.of("type", "json_object")
        );

        try {
            String response = restClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseResponse(response, word);

        } catch (Exception e) {
            e.printStackTrace();
            return new WordResponse(word, "Error", "API Failed: " + e.getMessage(), "", new ArrayList<>());
        }
    }

    private WordResponse parseResponse(String rawJson, String originalWord) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            
            // Groq/OpenAI response path
            String contentString = root.path("choices").get(0)
                    .path("message")
                    .path("content").asText();

            JsonNode data = objectMapper.readTree(contentString);

            List<String> examples = new ArrayList<>();
            data.path("examples").forEach(node -> examples.add(node.asText()));

            return new WordResponse(
                originalWord,
                "Marathi",
                data.path("meaning").asText(),
                data.path("explanation").asText(),
                examples
            );

        } catch (Exception e) {
            return new WordResponse(originalWord, "Error", "Parser Error", "", new ArrayList<>());
        }
    }
}