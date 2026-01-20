package com.example.keeplearning.service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import com.example.keeplearning.entity.UserStatus;
import jakarta.transaction.Transactional;
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
    @Autowired
    private EmailService emailService;

    
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

        User newUser = userRepository.save(user);
        emailService.sendRegisterConfirmation(newUser.getEmail());

        return Optional.of(newUser);
    }

    public boolean saveUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            return false;
        }

        userRepository.save(user);
        return true;
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

    public boolean isUserPassword(long userID, String password) {
        return userRepository.findById(userID)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    public void updatePassword(long userID, String newPassword) {
        var user = userRepository.findById(userID).orElseThrow();
        user.setPasswordEncoded(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    //UserDetails implementieren

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                String.format("No user with email '%s'", email)
                        )
                );

        // automatisches entsperrren nach abgelaufener Sperre
        if (user.getStatus() == UserStatus.LOCKED
                && user.getLockedUntil() != null
                && LocalDateTime.now().isAfter(user.getLockedUntil())) {

            user.setStatus(UserStatus.ACTIVE);
            user.setLockedUntil(null);
            user.setLockReason(null);
        }

        return user;
    }


}
