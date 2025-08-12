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

class UserSubscriptionsListResponseDTOTest {

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
        UserSubscriptionsListResponseDTO dto = new UserSubscriptionsListResponseDTO();
        assertNull(dto.getAuthors());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        List<UserInfoResponseDTO> authors = Collections.singletonList(buildUser("A", "B", "a@b.c"));
        UserSubscriptionsListResponseDTO dto = new UserSubscriptionsListResponseDTO(authors);
        assertEquals(authors, dto.getAuthors());
    }

    @Test
    void testSettersAndGetters() {
        UserSubscriptionsListResponseDTO dto = new UserSubscriptionsListResponseDTO();
        List<UserInfoResponseDTO> authors = Arrays.asList(
            buildUser("A", "B", "a@b.c"),
            buildUser("C", "D", "c@d.e")
        );
        dto.setAuthors(authors);
        assertEquals(authors, dto.getAuthors());
    }

    @Test
    void testEqualsHashCodeAndToString() {
        List<UserInfoResponseDTO> authors = Collections.singletonList(buildUser("A", "B", "a@b.c"));
        UserSubscriptionsListResponseDTO d1 = new UserSubscriptionsListResponseDTO(authors);
        UserSubscriptionsListResponseDTO d2 = new UserSubscriptionsListResponseDTO(authors);
        UserSubscriptionsListResponseDTO d3 = new UserSubscriptionsListResponseDTO(Collections.emptyList());

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertNotEquals(d1, d3);
        assertNotEquals(d1.hashCode(), d3.hashCode());

        String ts = d1.toString();
        assertNotNull(ts);
        assert ts.contains("A");
    }
} 