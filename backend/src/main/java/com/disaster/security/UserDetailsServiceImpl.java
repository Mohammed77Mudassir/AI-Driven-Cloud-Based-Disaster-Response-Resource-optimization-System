package com.disaster.security;

import com.disaster.entity.User;
import com.disaster.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Accept both the username and the account email as the login identifier,
        // matching the "Username or Email" label shown on the login form.
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail).orElse(null));
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + usernameOrEmail);
        }
        return UserDetailsImpl.build(user);
    }
}
