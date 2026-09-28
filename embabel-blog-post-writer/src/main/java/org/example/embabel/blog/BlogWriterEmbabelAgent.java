package org.example.embabel.blog;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import org.example.embabel.blog.model.BlogDraft;
import org.example.embabel.blog.model.PublishedPost;
import org.example.embabel.blog.model.ReviewedPost;
import org.example.embabel.blog.model.SEOTags;

@Agent(description = "Write and review a blog post about a given topic")
public class BlogWriterEmbabelAgent {

    @Action(description = "Write a first draft of the blog post")
    public BlogDraft writeDraft(UserInput userInput, Ai ai) {
        return ai.withDefaultLlm()
                .withId("blog-post-draft-writer")
                .creating(BlogDraft.class)
                .fromPrompt("""
                Write a short (max 200 words) blog post about %s.
                Keep it practical and beginner-friendly.
                """.formatted(userInput.getContent()));
    }

    @Action(description = "Review and improve the draft")
    public ReviewedPost reviewDraft(BlogDraft draft, Ai ai) {
        return ai.withLlmByRole("reviewer")
                .withId("blog-post-reviewer")
                .creating(ReviewedPost.class)
                .fromPrompt("""
                Review, improve and provide feedback on this blog post:
                <TITLE>%s</TITLE>
                <CONTENT>%s</CONTENT>
                """.formatted(draft.title(), draft.content()));
    }

    @Action(description = "Generate SEO metadata for the blog post")
    public SEOTags generateSEOTags(BlogDraft draft, Ai ai) {
        return ai.withDefaultLlm()
                .withId("blog-post-seo-tagger")
                .creating(SEOTags.class)
                .fromPrompt("""
                Generate SEO metadata for this blog post:
                <TITLE>%s</TITLE>
                <CONTENT>%s</CONTENT>

                Provide a short meta description under 100 characters,
                5-8 relevant keywords (comma separated), and a URL-friendly slug.
                """.formatted(draft.title(), draft.content()));
    }

    @Action(description = "Assemble the final publish-ready post")
    @AchievesGoal(description = "A polished, SEO-tagged blog post ready to publish")
    public PublishedPost publishPost(ReviewedPost reviewed, SEOTags seo) {
        return new PublishedPost(reviewed.title(), reviewed.content(), reviewed.feedback(), seo);
    }
}