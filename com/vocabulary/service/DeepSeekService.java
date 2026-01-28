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
    	
    	String prompt = String.format(
    	        "You are a bilingual English-Marathi dictionary. Analyze the word '%s'. " +
    	        "1. Identify if the word is English or Marathi. " +
    	        "2. If it's English, provide the 'meaning' in Marathi. If it's Marathi, provide the 'meaning' in English. " +
    	        "3. Provide a detailed 'explanation' in Marathi. " +
    	        "4. Provide 3 'examples' (objects with 'marathi' and 'english' keys) showing the word in use. " +
    	        "Return a STRICT JSON object only.", word);

    	    Map<String, Object> requestBody = Map.of(
    	        "model", apiModel,
    	        "messages", List.of(
    	            Map.of("role", "system", "content", "You are a helpful assistant that outputs JSON."),
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
            String contentString = root.path("choices").get(0).path("message").path("content").asText();
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

            // Logic: Return the meaning provided by the AI. 
            // You might want to update your DTO to include a 'detectedLanguage' field 
            // if your UI needs to know which way the translation went.
            return new WordResponse(
                originalWord,
                "Bilingual", // Changed from "Marathi" to "Bilingual"
                data.path("meaning").asText(),
                data.path("explanation").asText(),
                examples
            );

        } catch (Exception e) {
            return new WordResponse(originalWord, "Error", "Parser Error: " + e.getMessage(), "", new ArrayList<>());
        }
    }
}