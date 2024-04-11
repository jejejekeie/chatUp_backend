package com.chatup.backend.repositories;

import com.chatup.backend.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    @Override
    Optional<User> findById(String id);
    List<User> findUsersByEmail(Set<String> email);
    Optional<Object> findUserByEmail(String memberId);
}
