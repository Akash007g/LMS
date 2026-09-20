package com.example.lms.service;

import com.example.lms.entity.Role;
import com.example.lms.entity.User;
import com.example.lms.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    @Mock
    private UserRepository repository;

    @Test
    void loadUserByUsernameMapsUserDetailsAndRole() {
        User user = User.builder()
                .email("student@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .enabled(true)
                .build();
        when(repository.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(user));
        CustomUserDetailsService service = new CustomUserDetailsService(repository);

        UserDetails details = service.loadUserByUsername("student@example.com");

        assertEquals("student@example.com", details.getUsername());
        assertEquals("encoded-password", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsernameThrowsWhenUserDoesNotExist() {
        when(repository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());
        CustomUserDetailsService service = new CustomUserDetailsService(repository);

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing@example.com"));
    }
}
