package com.chatup.backend.services;

import com.chatup.backend.models.Chat;
import com.chatup.backend.models.User;
import com.chatup.backend.repositories.ChatRepository;
import com.chatup.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    public ChatService(ChatRepository chatRepository, UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    public Optional<String> getChatId (
            Set<String> members,
            boolean createNewChatIfNotExists
    ) {
        return chatRepository
                .findChatBy(members)
                .map(Chat::getChatId)
                .or(() -> {
                    if (createNewChatIfNotExists) {
                        var chatId = createChatId(members);
                        return Optional.of(chatId);
                    }

                    return Optional.empty();
                });
    }

    public List<Chat> getChatsByMemberId(String memberId) {
        return chatRepository.findChatsByMemberId(memberId);
    }

    private String createChatId(Set<String> members) {
        List<String> sortedMembers = new ArrayList<>(members);
        Collections.sort(sortedMembers);
        String chatId = String.join("-", sortedMembers);
        Chat chat = Chat.builder()
                .chatId(chatId)
                .members(new HashSet<>(sortedMembers))
                .build();
        return chatId;
    }

    public Chat addUserToChat(String chatId, String userId) {
        Chat chat = chatRepository.findChatById(chatId).orElseThrow(
                () -> new IllegalArgumentException("Chat not found")
        );
        chat.getMembers().add(userId);
        return chatRepository.save(chat);
    }

    public Chat removeUserFromChat(String chatId, String userId) {
        Chat chat = chatRepository.findChatById(chatId).orElseThrow();
        chat.getMembers().remove(userId);
        return chatRepository.save(chat);
    }

    public List<User> getChatMembers(String chatId) {
        Set<String> members = chatRepository.findChatById(chatId).orElseThrow().getMembers();
        return userRepository.findUsersByEmail(members);
    }

    public Chat createChat(Chat chat) {
        return chatRepository.save(chat);
    }

    public List<Chat> findChatsByName(String chatName) {
        return chatRepository.findChatsByName(chatName);
    }
}
