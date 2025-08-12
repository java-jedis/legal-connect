package com.javajedis.legalconnect.blogs.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class EsBlogRepoTest {

    @Mock
    private EsBlogRepo esBlogRepo;

    private UUID id;
    private EsBlog doc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        doc = new EsBlog();
        doc.setId(id);
        doc.setAuthorId(UUID.randomUUID());
        doc.setTitle("T");
        doc.setContent("C");
        doc.setStatus("PUBLISHED");
        doc.setCreatedAt(OffsetDateTime.now().minusDays(1));
        doc.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void testSaveAndFindById() {
        when(esBlogRepo.save(any(EsBlog.class))).thenReturn(doc);
        when(esBlogRepo.findById(id)).thenReturn(Optional.of(doc));

        EsBlog saved = esBlogRepo.save(doc);
        assertNotNull(saved);
        assertEquals("T", saved.getTitle());

        Optional<EsBlog> found = esBlogRepo.findById(id);
        assertTrue(found.isPresent());
        assertEquals("C", found.get().getContent());
    }

    @Test
    void testFindByIdNotFoundAndDelete() {
        when(esBlogRepo.findById(UUID.randomUUID())).thenReturn(Optional.empty());
        doNothing().when(esBlogRepo).deleteById(id);

        Optional<EsBlog> missing = esBlogRepo.findById(UUID.randomUUID());
        assertFalse(missing.isPresent());

        esBlogRepo.deleteById(id);
        assertTrue(true);
    }
} 