package com.example.keeplearning.entity;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;

@Entity
@Table(name = "benutzer")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(unique=true)
    private String email;
    private String password;
    private String role;
    private String googleRefreshToken;
    @Column(nullable = false)
    private boolean verified = false;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    private LocalDateTime lockedUntil;
    private String lockReason;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPasswordEncoded(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGoogleRefreshToken() {
        return googleRefreshToken;
    }

    public void setGoogleRefreshToken(String googleRefreshToken) {
        this.googleRefreshToken = googleRefreshToken;
    }

    public boolean isVerified() { return verified; }

    public void setVerified(boolean verified) { this.verified = verified; }

    public User() {}
    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    //////////////////////////////////////////////////////////////////////////
    //// UserDetails implementation

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Rolle
        authorities.add(
                new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
        );

        // Verifizierungsstatus
        if (verified) {
            authorities.add(
                    new SimpleGrantedAuthority("VERIFIED")
            );
        }

        return authorities;
    }




    @Override
    public String getUsername() {
        // email as login username
        return email;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public String getLockReason() {
        return lockReason;
    }

    public void setLockReason(String lockReason) {
        this.lockReason = lockReason;
    }

    public boolean isLocked() {
        if (status != UserStatus.LOCKED) {
            return false;
        }

        if (lockedUntil == null) {
            return true; // unbefristet gesperrt
        }

        return LocalDateTime.now().isBefore(lockedUntil);
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    //automatisch nach Ablauf der Sperre den Account wieder freigeben
    @Override
    public boolean isAccountNonLocked() {
        if (status == UserStatus.LOCKED && lockedUntil != null) {
            if (LocalDateTime.now().isAfter(lockedUntil)) {
                status = UserStatus.ACTIVE;
                lockedUntil = null;
            }
        }
        return !isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    //soft delete, ist nicht wirklich aus Tabelle gelöscht
    @Override
    public boolean isEnabled() {
        return status != UserStatus.DELETED;
    }
}
