package org.example.langgraph.blog;

import org.example.langgraph.blog.model.PublishedPost;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/blog")
public class BlogController {

    private final BlogWriterLangGraphService blogWriterLangGraphService;

    public BlogController(BlogWriterLangGraphService blogWriterLangGraphService) {
        this.blogWriterLangGraphService = blogWriterLangGraphService;
    }

    @PostMapping("/write")
    public PublishedPost write(@RequestBody BlogRequest request) {
        return blogWriterLangGraphService.invoke(request.topic);
    }

    public record BlogRequest(String topic) {};
}