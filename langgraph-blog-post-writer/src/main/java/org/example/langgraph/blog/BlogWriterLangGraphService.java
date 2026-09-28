package org.example.langgraph.blog;

import org.example.langgraph.blog.model.*;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

import org.bsc.langgraph4j.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Service
public class BlogWriterLangGraphService {

    private final ChatLanguageModel llm;
    private final CompiledGraph<BlogWriterState> graph;

    public BlogWriterLangGraphService(@Value("${llm.anthropic.api-key}") String apiKey,
                                      @Value("${llm.anthropic.model}") String modelName,
                                      @Value("${llm.anthropic.temperature}") double temperature) throws GraphStateException {
        this.llm = AnthropicChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .build();

        this.graph = buildGraph();
    }

    private CompiledGraph<BlogWriterState> buildGraph() throws GraphStateException {
        StateGraph<BlogWriterState> stateGraph = new StateGraph<>(BlogWriterState::new);

        // Step 1: writeDraft
        stateGraph.addNode("writeDraft", node_async(state -> {
            String topic = state.topic();
            String prompt = """
                    Write a short (max 200 words) blog post about %s.
                    Keep it practical and beginner-friendly.
                    
                    Format clearly with <TITLE></TITLE><CONTENT></CONTENT>.
                    """.formatted(topic);

            String response = llm.generate(prompt);
            return Map.of("draft", parseDraft(response));
        }));

        // Step 2: reviewDraft (Runs in parallel with Step 3)
        stateGraph.addNode("reviewDraft", node_async(state -> {
            Draft draft = state.draft();
            String prompt = """
                    Review, improve and provide feedback on this blog post:
                    <TITLE>%s</TITLE>
                    <CONTENT>%s</CONTENT>
                    
                    Format clearly with <TITLE></TITLE><CONTENT></CONTENT><FEEDBACK></FEEDBACK>.
                    """.formatted(draft.title(), draft.content());

            String response = llm.generate(prompt);
            var reviewedDraft = parseReviewedDraft(response);
            return Map.of("reviewedDraft", reviewedDraft);
        }));

        // Step 3: generateSEOTags (Runs in parallel with Step 2)
        stateGraph.addNode("generateSEOTags", node_async(state -> {
            Draft draft = state.draft();
            String prompt = """
                    Generate SEO metadata for this blog post:
                    <TITLE>%s</TITLE>
                    <CONTENT>%s</CONTENT>
                    
                    Provide a short meta description under 100 characters,
                    5-8 relevant keywords (comma separated), and a URL-friendly slug.
                    
                    Format clearly with <META_DESCRIPTION></META_DESCRIPTION><KEYWORDS></KEYWORDS><SLUG></SLUG>.
                    """.formatted(draft.title(), draft.content());

            String response = llm.generate(prompt);
            return Map.of("seoMetadata", parseSeo(response));
        }));

        // Step 4: publishPost (Fan-in node joining reviewDraft + generateSEOTags)
        stateGraph.addNode("publishPost", node_async(state -> {
            ReviewedDraft reviewed = state.reviewedDraft();
            SeoMetadata seo = state.seoMetadata();

            PublishedPost post = new PublishedPost(
                    reviewed.title(),
                    reviewed.content(),
                    reviewed.feedback(),
                    seo
            );

            return Map.of("publishedPost", post);
        }));

        // Wiring Graph Topology:
        stateGraph.addEdge(START, "writeDraft");

        // Fork to step 2 & step 3 concurrently
        stateGraph.addEdge("writeDraft", "reviewDraft");
        stateGraph.addEdge("writeDraft", "generateSEOTags");

        // Join both branches back at step 4
        stateGraph.addEdge("reviewDraft", "publishPost");
        stateGraph.addEdge("generateSEOTags", "publishPost");

        stateGraph.addEdge("publishPost", END);

        return stateGraph.compile();
    }

    public PublishedPost invoke(String topic) {
        Map<String, Object> inputs = Map.of("topic", topic);

        RunnableConfig config = RunnableConfig.builder()
                .threadId("blog-thread-101")
                .build();

        BlogWriterState finalState = graph.invoke(GraphInput.args(inputs), config)
                .orElseThrow(() -> new IllegalStateException("Graph execution produced no result"));

        return finalState.publishedPost();
    }

    private ReviewedDraft parseReviewedDraft(String response) {
        String title = response.contains("<TITLE>") ? response.split("<TITLE>")[1].split("</TITLE>")[0].trim() : "Untitled";
        String content = response.contains("<CONTENT>") ? response.split("<CONTENT>")[1].split("</CONTENT>")[0].trim() : response;
        String feedback = response.contains("<FEEDBACK>") ? response.split("<FEEDBACK>")[1].split("</FEEDBACK>")[0].trim() : "No feedback provided.";
        return new ReviewedDraft(title, content, feedback);
    }

    private Draft parseDraft(String response) {
        String title = response.contains("<TITLE>") ? response.split("<TITLE>")[1].split("</TITLE>")[0].trim() : "Untitled";
        String content = response.contains("<CONTENT>") ? response.split("<CONTENT>")[1].split("</CONTENT>")[0].trim() : response;
        return new Draft(title, content);
    }

    private SeoMetadata parseSeo(String response) {
        String metaDescription = response.contains("<META_DESCRIPTION>") ? response.split("<META_DESCRIPTION>")[1].split("</META_DESCRIPTION>")[0].trim() : "A beginner guide to the requested topic.";
        List<String> keywords = response.contains("<KEYWORDS>") ? List.of(response.split("<KEYWORDS>")[1].split("</KEYWORDS>")[0].trim().split(",")) : List.of("java", "spring-boot", "langgraph4j", "anthropic");
        String slug = response.contains("<SLUG>") ? response.split("<SLUG>")[1].split("</SLUG>")[0].trim() : "practical-guide-slug";
        return new SeoMetadata(metaDescription, keywords, slug);
    }
}