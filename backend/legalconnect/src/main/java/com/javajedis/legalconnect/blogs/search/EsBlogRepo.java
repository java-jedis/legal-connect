package com.javajedis.legalconnect.blogs.search;

import java.util.UUID;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EsBlogRepo extends ElasticsearchRepository<EsBlog, UUID> {
} 