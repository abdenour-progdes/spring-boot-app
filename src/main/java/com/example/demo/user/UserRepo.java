package com.example.demo.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo  extends JpaRepository<AppUser, Long> {
    AppUser findByUsername(String username);
}