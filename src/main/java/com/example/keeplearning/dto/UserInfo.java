package com.example.keeplearning.dto;

public class UserInfo {

    public String Name;
    public String Email;
    public String Password;
    public String Role;
    
    public UserInfo() {}
    public UserInfo(String name, String email, String password, String role) {
        Name = name;
        Email = email;
        Password = password;
        Role = role;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }
}
