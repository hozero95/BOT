package com.example.bot.core.security.util;

import com.example.bot.biz.entity.User;
import com.example.bot.biz.repository.AuthRepository;
import com.example.bot.biz.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailService Custom Service
 * with Spring Security Core
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final AuthRepository authRepository;

    public CustomUserDetailsService(UserRepository userRepository, AuthRepository authRepository) {
        this.userRepository = userRepository;
        this.authRepository = authRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsercd(username);

        if (user != null) {
            return new CustomUserDetails(user, authRepository);
        }

        return null;
    }
}
