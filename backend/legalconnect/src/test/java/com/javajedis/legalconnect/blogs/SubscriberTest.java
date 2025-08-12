package com.javajedis.legalconnect.blogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class SubscriberTest {

    private Subscriber subscriber;
    private User author;
    private User follower;
    private UUID subId;

    @BeforeEach
    void setUp() {
        subId = UUID.randomUUID();

        author = new User();
        author.setId(UUID.randomUUID());
        author.setFirstName("Author");
        author.setLastName("One");
        author.setEmail("author@example.com");
        author.setRole(Role.USER);
        author.setPassword("pw");
        author.setEmailVerified(true);

        follower = new User();
        follower.setId(UUID.randomUUID());
        follower.setFirstName("Follower");
        follower.setLastName("Two");
        follower.setEmail("follower@example.com");
        follower.setRole(Role.USER);
        follower.setPassword("pw");
        follower.setEmailVerified(true);

        subscriber = new Subscriber();
        subscriber.setId(subId);
        subscriber.setAuthor(author);
        subscriber.setSubscriber(follower);
        subscriber.setCreatedAt(OffsetDateTime.now().minusDays(1));
        subscriber.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void testDefaultConstructor() {
        Subscriber s = new Subscriber();
        assertNotNull(s);
        assertNull(s.getId());
        assertNull(s.getAuthor());
        assertNull(s.getSubscriber());
        assertNull(s.getCreatedAt());
        assertNull(s.getUpdatedAt());
    }

    @Test
    void testGettersAndSetters() {
        UUID newId = UUID.randomUUID();
        subscriber.setId(newId);
        assertEquals(newId, subscriber.getId());

        User newAuthor = new User();
        newAuthor.setId(UUID.randomUUID());
        subscriber.setAuthor(newAuthor);
        assertEquals(newAuthor, subscriber.getAuthor());

        User newFollower = new User();
        newFollower.setId(UUID.randomUUID());
        subscriber.setSubscriber(newFollower);
        assertEquals(newFollower, subscriber.getSubscriber());

        OffsetDateTime created = OffsetDateTime.now().minusDays(3);
        OffsetDateTime updated = OffsetDateTime.now();
        subscriber.setCreatedAt(created);
        subscriber.setUpdatedAt(updated);
        assertEquals(created, subscriber.getCreatedAt());
        assertEquals(updated, subscriber.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        OffsetDateTime now = OffsetDateTime.now();
        Subscriber s1 = new Subscriber(subId, author, follower, now, now);
        Subscriber s2 = new Subscriber(subId, author, follower, now, now);
        Subscriber s3 = new Subscriber(UUID.randomUUID(), author, follower, now, now);

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
        assertNotEquals(s1, s3);
        assertNotEquals(s1.hashCode(), s3.hashCode());
    }

    @Test
    void testToString() {
        String ts = subscriber.toString();
        assertNotNull(ts);
        assertTrue(ts.contains("Author"));
        assertTrue(ts.contains("Follower"));
    }
} 