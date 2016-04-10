package com.policat.cat.repositories;

import com.policat.cat.entities.Quiz;
import com.policat.cat.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    public User findByUsername(String username);
}
