package com.vocabulary.dto;

import java.util.List;

public record WordResponse(
	    String word,
	    String language,
	    String meaning,
	    String explanation,
	    List<String> examples
	) {}
