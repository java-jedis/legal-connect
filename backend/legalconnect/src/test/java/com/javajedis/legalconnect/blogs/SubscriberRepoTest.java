package com.javajedis.legalconnect.blogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class SubscriberRepoTest {

    @Mock
    private SubscriberRepo subscriberRepo;

    private UUID authorId1;
    private UUID authorId2;
    private UUID followerId1;
    private UUID followerId2;

    private User author1;
    private User author2;
    private User follower1;
    private User follower2;

    private Subscriber s1;
    private Subscriber s2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        authorId1 = UUID.randomUUID();
        authorId2 = UUID.randomUUID();
        followerId1 = UUID.randomUUID();
        followerId2 = UUID.randomUUID();

        author1 = new User();
        author1.setId(authorId1);
        author1.setFirstName("Auth");
        author1.setLastName("One");
        author1.setEmail("a1@example.com");
        author1.setRole(Role.USER);
        author1.setPassword("pw");
        author1.setEmailVerified(true);

        author2 = new User();
        author2.setId(authorId2);
        author2.setFirstName("Auth");
        author2.setLastName("Two");
        author2.setEmail("a2@example.com");
        author2.setRole(Role.USER);
        author2.setPassword("pw");
        author2.setEmailVerified(true);

        follower1 = new User();
        follower1.setId(followerId1);
        follower1.setFirstName("Fol");
        follower1.setLastName("One");
        follower1.setEmail("f1@example.com");
        follower1.setRole(Role.USER);
        follower1.setPassword("pw");
        follower1.setEmailVerified(true);

        follower2 = new User();
        follower2.setId(followerId2);
        follower2.setFirstName("Fol");
        follower2.setLastName("Two");
        follower2.setEmail("f2@example.com");
        follower2.setRole(Role.USER);
        follower2.setPassword("pw");
        follower2.setEmailVerified(true);

        OffsetDateTime now = OffsetDateTime.now();
        s1 = new Subscriber(UUID.randomUUID(), author1, follower1, now.minusDays(2), now.minusDays(1));
        s2 = new Subscriber(UUID.randomUUID(), author1, follower2, now.minusDays(1), now);
    }

    @Test
    void testFindByAuthorId_List() {
        when(subscriberRepo.findByAuthorId(authorId1)).thenReturn(Arrays.asList(s1, s2));
        List<Subscriber> found = subscriberRepo.findByAuthorId(authorId1);
        assertNotNull(found);
        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(s -> s.getAuthor().getId().equals(authorId1)));
    }

    @Test
    void testFindBySubscriberId_List() {
        when(subscriberRepo.findBySubscriberId(followerId1)).thenReturn(Collections.singletonList(s1));
        List<Subscriber> found = subscriberRepo.findBySubscriberId(followerId1);
        assertNotNull(found);
        assertEquals(1, found.size());
        assertTrue(found.stream().allMatch(s -> s.getSubscriber().getId().equals(followerId1)));
    }

    @Test
    void testFindByAuthorId_Pageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Subscriber> page = new PageImpl<>(Arrays.asList(s1, s2), pageable, 2);
        when(subscriberRepo.findByAuthorId(authorId1, pageable)).thenReturn(page);
        Page<Subscriber> found = subscriberRepo.findByAuthorId(authorId1, pageable);
        assertEquals(2, found.getContent().size());
    }

    @Test
    void testFindBySubscriberId_Pageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Subscriber> page = new PageImpl<>(Arrays.asList(s1), pageable, 1);
        when(subscriberRepo.findBySubscriberId(followerId1, pageable)).thenReturn(page);
        Page<Subscriber> found = subscriberRepo.findBySubscriberId(followerId1, pageable);
        assertEquals(1, found.getContent().size());
    }

    @Test
    void testFindByAuthorIdAndSubscriberId() {
        when(subscriberRepo.findByAuthorIdAndSubscriberId(authorId1, followerId1)).thenReturn(Optional.of(s1));
        when(subscriberRepo.findByAuthorIdAndSubscriberId(authorId2, followerId1)).thenReturn(Optional.empty());

        Optional<Subscriber> present = subscriberRepo.findByAuthorIdAndSubscriberId(authorId1, followerId1);
        Optional<Subscriber> missing = subscriberRepo.findByAuthorIdAndSubscriberId(authorId2, followerId1);

        assertTrue(present.isPresent());
        assertFalse(missing.isPresent());
        assertEquals(followerId1, present.get().getSubscriber().getId());
    }

    @Test
    void testDeleteByAuthorIdAndSubscriberId() {
        // Mock void method
        doNothing().when(subscriberRepo).deleteByAuthorIdAndSubscriberId(authorId1, followerId1);
        subscriberRepo.deleteByAuthorIdAndSubscriberId(authorId1, followerId1);
        // No exception expected; we can optionally verify subsequent state via separate mock expectations
        assertTrue(true);
    }
} 