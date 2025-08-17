package com.javajedis.legalconnect.blogs.search;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Data;

@Data
@Document(indexName = "blogs")
public class EsBlog {
    @Id
    private UUID id;

    @Field(type = FieldType.Keyword)
    private UUID authorId;

    @Field(type = FieldType.Text)
    private String authorFirstName;

    @Field(type = FieldType.Text)
    private String authorLastName;

    @Field(type = FieldType.Keyword)
    private String authorEmail;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private OffsetDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private OffsetDateTime updatedAt;
} 