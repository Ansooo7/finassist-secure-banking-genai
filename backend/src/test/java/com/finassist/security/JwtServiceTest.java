package com.finassist.security;

import com.finassist.model.Role;
import com.finassist.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // 512-bit base64 key
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970337336763979244226452948404D6351655468576D5A7134743777217A25432A");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L);
    }

    @Test
    void testTokenGenerationAndValidation() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("oliver")
                .passwordHash("hashed")
                .email("oliver@test.co.uk")
                .fullName("Oliver Twist")
                .role(Role.ROLE_CUSTOMER)
                .enabled(true)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertEquals("oliver", jwtService.extractUsername(token));
        assertEquals("ROLE_CUSTOMER", jwtService.extractRole(token));
    }
}
