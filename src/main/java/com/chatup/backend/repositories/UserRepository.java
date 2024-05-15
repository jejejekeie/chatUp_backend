package com.chatup.backend.repositories;

import com.chatup.backend.models.User;
import com.mongodb.lang.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    @NonNull
    @Override
    Optional<User> findById(@NonNull String userId);
    @Query("{ '_id' : { $in: ?0 } }")
    @NonNull
    List<User> findUsersByIds(Set<String> ids);
    List<User> findByUsernameContainingOrEmailContaining(String username, String email);
}
