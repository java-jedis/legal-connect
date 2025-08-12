package com.javajedis.legalconnect.blogs.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.javajedis.legalconnect.blogs.Blog;
import com.javajedis.legalconnect.blogs.BlogStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsBlogService {

    private final EsBlogRepo esBlogRepo;
    private final ElasticsearchOperations elasticOps;

    public void index(Blog blog) {
        try {
            esBlogRepo.save(mapToEsBlog(blog));
        } catch (Exception e) {
            log.error("Failed to index blog {} in Elasticsearch", blog.getId(), e);
        }
    }

    public void update(Blog blog) {
        index(blog);
    }

    public void delete(UUID blogId) {
        try {
            esBlogRepo.deleteById(blogId);
        } catch (Exception e) {
            log.error("Failed to delete blog {} from Elasticsearch", blogId, e);
        }
    }

    public Optional<EsBlog> getById(UUID blogId) {
        try {
            return esBlogRepo.findById(blogId);
        } catch (Exception e) {
            log.error("Failed to fetch blog {} from Elasticsearch", blogId, e);
            return Optional.empty();
        }
    }

    public SearchPage searchPublishedBlogs(String query, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));

            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> b
                            .must(m -> m.multiMatch(mm -> mm
                                    .query(query)
                                    .fields("title", "content")
                                    .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                            ))
                            .filter(f -> f.term(t -> t.field("status").value(BlogStatus.PUBLISHED.name())))
                    ))
                    .withPageable(pageable)
                    .build();

            SearchHits<EsBlog> hits = elasticOps.search(nativeQuery, EsBlog.class);
            List<SearchResult> results = new ArrayList<>();
            for (SearchHit<EsBlog> hit : hits) {
                EsBlog doc = hit.getContent();
                String hlTitle = makeHighlight(doc.getTitle(), query, 60);
                String hlContent = makeHighlight(doc.getContent(), query, 100);
                results.add(new SearchResult(doc, hlTitle, hlContent));
            }
            long total = hits.getTotalHits();
            return new SearchPage(results, total);
        } catch (Exception e) {
            log.error("Failed to search blogs in Elasticsearch", e);
            return new SearchPage(List.of(), 0L);
        }
    }

    private String makeHighlight(String text, String query, int context) {
        if (text == null || text.isEmpty() || query == null || query.isEmpty()) return null;
        String lower = text.toLowerCase();
        String q = query.toLowerCase();
        int idx = lower.indexOf(q);
        if (idx < 0) return null;
        int start = Math.max(0, idx - context);
        int end = Math.min(text.length(), idx + q.length() + context);
        String prefix = start > 0 ? "... " : "";
        String suffix = end < text.length() ? " ..." : "";
        return prefix + text.substring(start, idx) + "<em>" + text.substring(idx, idx + q.length()) + "</em>" + text.substring(idx + q.length(), end) + suffix;
    }

    public record SearchResult(EsBlog blog, String highlightedTitle, String highlightedContent) {}
    public record SearchPage(List<SearchResult> results, long total) {}

    private EsBlog mapToEsBlog(Blog blog) {
        EsBlog es = new EsBlog();
        es.setId(blog.getId());
        es.setAuthorId(blog.getAuthor().getId());
        es.setAuthorFirstName(blog.getAuthor().getFirstName());
        es.setAuthorLastName(blog.getAuthor().getLastName());
        es.setAuthorEmail(blog.getAuthor().getEmail());
        es.setTitle(blog.getTitle());
        es.setContent(blog.getContent());
        es.setStatus(blog.getStatus() != null ? blog.getStatus().name() : BlogStatus.DRAFT.name());
        es.setCreatedAt(blog.getCreatedAt());
        es.setUpdatedAt(blog.getUpdatedAt());
        return es;
    }
} 