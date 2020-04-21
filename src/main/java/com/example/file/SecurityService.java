package com.example.file;

public interface SecurityService {
    String findLoggedInUsername();

    void autoLogin(String username, String password);
}