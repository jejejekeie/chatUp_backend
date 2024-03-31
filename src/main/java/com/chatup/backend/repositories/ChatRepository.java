package com.chatup.backend.repositories;

import com.chatup.backend.models.Chat;
import com.chatup.backend.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ChatRepository extends MongoRepository<Chat, String> {
    Optional<Chat> findChatBy(Set<String> miembrosId);
    List<Chat> findChatsByUserId(String userId);
    Optional<Chat> findChatById(String chat);

    List<User> findChatMembers(String chatId);
}
