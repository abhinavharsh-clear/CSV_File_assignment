package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    /* ---------- READ ---------- */
    @GetMapping
    public List<User> getAllUsers() {
        return service.getAllUsers();
    }

    /* ---------- UPDATE ---------- */
    @PutMapping("/{id}")
    public String updateUser(
            @PathVariable int id,
            @RequestParam String email,
            @RequestParam String name
    ) {
        return service.updateUser(id, email, name);
    }

    @PatchMapping("/{id}")
    public String pathUser(
        @PathVariable int id,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String name
    ){
        return service.patchUser(id,email,name);
    }


    /* ---------- DELETE ---------- */
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable int id) {
        return service.deleteUser(id);
    }
}

