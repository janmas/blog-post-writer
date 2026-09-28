package org.example.langgraph.blog.model;

import java.io.Serializable;
import java.util.List;

public record SeoMetadata(String metaDescription, List<String> keywords, String slug) implements Serializable {}
