package com.chatup.backend.repositories;

import com.chatup.backend.models.Chat;
import com.chatup.backend.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ChatRepository extends MongoRepository<Chat, String> {
    Optional<Chat> findChatBy(Set<String> miembrosId);
    Optional<Chat> findChatById(String chatId);
    @Query("{'members': ?0}")
    List<Chat> findChatsByMemberId(String memberId);

    List<Chat> findChatsByName(String chatName);
}
