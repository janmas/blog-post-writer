package org.example.langgraph.blog;

import org.example.langgraph.blog.model.*;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

public class BlogWriterState extends AgentState {

    public BlogWriterState(Map<String, Object> initData) {
        super(initData);
    }

    public String topic() {
        return value("topic").map(Object::toString).orElse("");
    }

    public Draft draft() {
        return (Draft) value("draft").orElse(null);
    }

    public ReviewedDraft reviewedDraft() {
        return (ReviewedDraft) value("reviewedDraft").orElse(null);
    }

    public SeoMetadata seoMetadata() {
        return (SeoMetadata) value("seoMetadata").orElse(null);
    }

    public PublishedPost publishedPost() {
        return (PublishedPost) value("publishedPost").orElse(null);
    }
}
