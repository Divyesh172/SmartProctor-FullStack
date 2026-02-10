package com.smartproctor.backend.service;

import com.smartproctor.backend.model.Professor;
import com.smartproctor.backend.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ProfessorRepository professorRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Professor professor = professorRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Convert our Professor model to Spring Security's UserDetails
        return new org.springframework.security.core.userdetails.User(
                professor.getEmail(),
                professor.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + professor.getRole().name()))
        );
    }
}