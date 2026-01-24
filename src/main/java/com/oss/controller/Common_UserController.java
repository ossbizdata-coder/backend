package com.oss.controller;
import com.oss.model.User;
import com.oss.service.UserService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/users")
public class Common_UserController {
    private final UserService service;
    public Common_UserController(UserService service) {
        this.service = service;
    }
    @GetMapping
    public List<User> getAll() {
        return service.getAllUsers();
    }
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return service.getUser(id);
    }
    @PostMapping
    public User create(@RequestBody User user) {
        return service.saveUser(user);
    }
    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        return service.updateUser(id, user);
    }
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.deleteUser(id);
        return "User deleted.";
    }
}