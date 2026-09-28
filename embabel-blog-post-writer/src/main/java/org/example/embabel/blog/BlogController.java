package org.example.embabel.blog;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.agent.core.Verbosity;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.embabel.blog.model.PublishedPost;

@RestController
@RequestMapping("/api/v1/blog")
public class BlogController {

    private final AgentPlatform agentPlatform;

    public BlogController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @PostMapping("/write")
    public PublishedPost write(@RequestBody BlogRequest request) {
        var options = new ProcessOptions()
                .withVerbosity(
                    new Verbosity()
                        .withShowPlanning(true)
                        .withShowPrompts(true)
                );
        var invocation = AgentInvocation
                .builder(agentPlatform)
                .options(options)
                .build(PublishedPost.class);

        return invocation.invoke(new UserInput(request.topic()));
    }

    public record BlogRequest(String topic) {}
}