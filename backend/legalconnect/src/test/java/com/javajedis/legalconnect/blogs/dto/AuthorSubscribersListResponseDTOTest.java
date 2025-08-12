package com.javajedis.legalconnect.blogs.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.UserInfoResponseDTO;

class AuthorSubscribersListResponseDTOTest {

    private UserInfoResponseDTO buildUser(String first, String last, String email) {
        UserInfoResponseDTO u = new UserInfoResponseDTO();
        u.setFirstName(first);
        u.setLastName(last);
        u.setEmail(email);
        u.setRole(Role.USER);
        u.setEmailVerified(true);
        u.setCreatedAt(OffsetDateTime.now());
        u.setUpdatedAt(OffsetDateTime.now());
        return u;
    }

    @Test
    void testDefaultConstructor() {
        AuthorSubscribersListResponseDTO dto = new AuthorSubscribersListResponseDTO();
        assertNull(dto.getSubscribers());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        List<UserInfoResponseDTO> subs = Collections.singletonList(buildUser("A", "B", "a@b.c"));
        AuthorSubscribersListResponseDTO dto = new AuthorSubscribersListResponseDTO(subs);
        assertEquals(subs, dto.getSubscribers());
    }

    @Test
    void testSettersAndGetters() {
        AuthorSubscribersListResponseDTO dto = new AuthorSubscribersListResponseDTO();
        List<UserInfoResponseDTO> subs = Arrays.asList(
            buildUser("A", "B", "a@b.c"),
            buildUser("C", "D", "c@d.e")
        );
        dto.setSubscribers(subs);
        assertEquals(subs, dto.getSubscribers());
    }

    @Test
    void testEqualsHashCodeAndToString() {
        List<UserInfoResponseDTO> subs = Collections.singletonList(buildUser("A", "B", "a@b.c"));
        AuthorSubscribersListResponseDTO d1 = new AuthorSubscribersListResponseDTO(subs);
        AuthorSubscribersListResponseDTO d2 = new AuthorSubscribersListResponseDTO(subs);
        AuthorSubscribersListResponseDTO d3 = new AuthorSubscribersListResponseDTO(Collections.emptyList());

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertNotEquals(d1, d3);
        assertNotEquals(d1.hashCode(), d3.hashCode());

        String ts = d1.toString();
        assertNotNull(ts);
        assert ts.contains("A");
    }
} 