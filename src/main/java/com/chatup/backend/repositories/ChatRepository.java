package com.chatup.backend.repositories;

import com.chatup.backend.models.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ChatRepository extends MongoRepository<Chat, String> {
    Optional<Chat> findChatByMembers(Set<String> miembrosId);
    Optional<Chat> findChatByChatId(String chatId);
    @Query("{'members': ?0}")
    List<Chat> findChatsByMemberId(String memberId);
    List<Chat> findChatsByName(String chatName);
    void deleteByChatId(String chatId);
}
