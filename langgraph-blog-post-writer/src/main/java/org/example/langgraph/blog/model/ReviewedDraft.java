package org.example.langgraph.blog.model;

import java.io.Serializable;

public record ReviewedDraft(String title, String content, String feedback) implements Serializable {}