package org.example.langgraph.blog.model;

import java.io.Serializable;

public record Draft(String title, String content) implements Serializable {}