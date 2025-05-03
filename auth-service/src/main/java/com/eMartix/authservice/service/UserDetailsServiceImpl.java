package com.eMartix.authservice.service;

import com.eMartix.authservice.model.User;
import com.eMartix.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Primary
    @Override
    @Transactional(readOnly = true)
    public User loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        return  userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("User or Email not found with username or email: " + usernameOrEmail)));
    }
}
