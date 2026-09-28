package org.example.langgraph.blog.model;

import java.io.Serializable;

public record PublishedPost(
        String title,
        String content,
        String reviewerFeedback,
        SeoMetadata seoMetadata
) implements Serializable {}
