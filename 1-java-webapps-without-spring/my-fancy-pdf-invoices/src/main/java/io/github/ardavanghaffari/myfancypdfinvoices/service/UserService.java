package io.github.ardavanghaffari.myfancypdfinvoices.service;

import io.github.ardavanghaffari.myfancypdfinvoices.model.User;

import java.util.UUID;

public class UserService {

    public User findById(String id) {
        String randomName = UUID.randomUUID().toString();
        return new User(id, randomName);
    }
}
