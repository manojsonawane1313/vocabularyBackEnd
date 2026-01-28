package com.vocabulary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabulary.dto.WordResponse;
import com.vocabulary.dto.WordResponse.ExampleDto; // Import the nested record

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

    @Value("${grok.api.model}")
    private String apiModel;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public DeepSeekService(RestClient.Builder builder, ObjectMapper objectMapper) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public WordResponse getWordData(String word) {
        // Updated prompt to request an array of objects
        String prompt = "You are a dictionary. For the English word '" + word + "', " +
                "provide a STRICT JSON response (no markdown) with these fields: " +
                "1. 'meaning' (in Marathi), " +
                "2. 'explanation' (in Marathi), " +
                "3. 'examples' (a list of 3 objects, each with 'marathi' and 'english' keys).";

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
            return new WordResponse(word, "Error", "API Failed: " + e.getMessage(), "", new ArrayList<>());
        }
    }

    private WordResponse parseResponse(String rawJson, String originalWord) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            
            // Extract the 'content' field from the AI provider's response
            String contentString = root.path("choices").get(0)
                    .path("message")
                    .path("content").asText();

            // Parse the actual dictionary data
            JsonNode data = objectMapper.readTree(contentString);

            List<ExampleDto> examples = new ArrayList<>();
            JsonNode examplesNode = data.path("examples");
            
            if (examplesNode.isArray()) {
                for (JsonNode node : examplesNode) {
                    examples.add(new ExampleDto(
                        node.path("marathi").asText(),
                        node.path("english").asText()
                    ));
                }
            }

            return new WordResponse(
                originalWord,
                "Marathi",
                data.path("meaning").asText(),
                data.path("explanation").asText(),
                examples
            );

        } catch (Exception e) {
            return new WordResponse(originalWord, "Error", "Parser Error: " + e.getMessage(), "", new ArrayList<>());
        }
    }
}