package com.example.keeplearning.service;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.keeplearning.dto.UserInfo;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    
    // returns the saved user object or empty optional with the user already exists
    public Optional<User> createUser(UserInfo info) {

        if (userRepository.existsByEmail(info.Email)) {
            return Optional.empty();
        }

        User user = new User(
            info.Name,
            info.Email,
            passwordEncoder.encode(info.Password),
            info.Role
        );

        return Optional.of(userRepository.save(user));
    }


    public Optional<User> getUser(Principal principal) {
        return getUserByEmail(principal.getName());
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserInfo getUserInfo(Principal principal) {
        var optUser = getUser(principal);

        if (optUser.isEmpty()) {
            return null;
        }

        User user = optUser.get();
        return new UserInfo(
            user.getName(),
            user.getEmail(),
            user.getPassword(),
            user.getRole()
        );
    }

    //////////////////////////////////////////////////////////////////////////
    //// UserDetails implementation

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
            .orElseThrow(
                () -> new UsernameNotFoundException(String.format("No user with email '%s'", email))
            );
    }

}
