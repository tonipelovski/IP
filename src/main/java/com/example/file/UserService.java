package com.example.file;


public interface UserService {
    void save(User user);

    User findByUsername(String username);
}