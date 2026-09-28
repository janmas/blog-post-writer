package org.example.embabel.blog.model;

import java.util.List;

public record SEOTags(String metaDescription, List<String> keywords, String slug) {}