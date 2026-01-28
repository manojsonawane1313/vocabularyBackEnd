package com.vocabulary.dto;

import java.util.List;

public record WordResponse(
    String word,
    String language,
    String meaning,
    String explanation,
    List<ExampleDto> examples
) {
    // Nested record to avoid creating a new file
    public record ExampleDto(
        String marathi,
        String english
    ) {}
}