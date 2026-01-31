package com.daghan.catalog.infrastructure.security;

import com.daghan.catalog.infrastructure.persistence.repository.SpringDataUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final SpringDataUserRepository userRepository;

    public DbUserDetailsService(SpringDataUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new User(
                userEntity.getUsername(),
                userEntity.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(userEntity.getRole())));
    }
}
