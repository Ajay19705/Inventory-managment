package com.Invnmgmt.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.Invnmgmt.exceptions.NotFoundException;
import com.Invnmgmt.models.User;
import com.Invnmgmt.repositories.UserRepository;

@Service

public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User Email Not Found"));

        return new AuthUser(user);
	
}
}
