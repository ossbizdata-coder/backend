package com.oss.service;

import com.oss.model.User;
import com.oss.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> getAllUsers() {
        return repo.findAll();
    }

    public User getUser(Long id) {
        return repo.findById(id).orElse(null);
    }

    public User saveUser(User user) {
        return repo.save(user);
    }

    public User updateUser(Long id, User user) {
        User existing = getUser(id);
        if (existing == null) return null;

        existing.setName(user.getName());
        existing.setEmail(user.getEmail());
        existing.setRole(user.getRole());
        return repo.save(existing);
    }

    public void deleteUser(Long id) {
        repo.deleteById(id);
    }
}
