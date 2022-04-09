package com.example.demo.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController @RequiredArgsConstructor @RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    @GetMapping("/users")
    public ResponseEntity<List<AppUSer>> getUsers(){
        return ResponseEntity.ok().body(userService.getUsers());
    }
}
