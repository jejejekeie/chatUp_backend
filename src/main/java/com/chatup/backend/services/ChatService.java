package com.chatup.backend.services;

import com.chatup.backend.models.Chat;
import com.chatup.backend.models.User;
import com.chatup.backend.repositories.ChatRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ChatService {
    private final ChatRepository chatRepository;
    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
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

    public List<Chat> getChatsByUserId(String userId) {
        return chatRepository.findChatsByUserId(userId);
    }

    private String createChatId(Set<String> members) {
        var chatId = String.format("%s_%s", members.toArray());

        Chat senderRecipient = Chat
                .builder()
                .chatId(chatId)
                .members(members)
                .build();

        Chat recipientSender = Chat
                .builder()
                .chatId(chatId)
                .members(Set.of(members.toArray()[1].toString()))
                .build();

        chatRepository.save(senderRecipient);
        chatRepository.save(recipientSender);

        return chatId;
    }

    public Chat addUserToChat(String chatId, String userId) {
        Chat chat = chatRepository.findChatById(chatId).orElseThrow();
        chat.getMembers().add(userId);
        return chatRepository.save(chat);
    }

    public Chat removeUserFromChat(String chatId, String userId) {
        Chat chat = chatRepository.findChatById(chatId).orElseThrow();
        chat.getMembers().remove(userId);
        return chatRepository.save(chat);
    }

    public List<User> getChatMembers(String chatId) {
        return chatRepository.findChatMembers(chatId);
    }

    public Chat createChat(Chat chat) {
        return chatRepository.save(chat);
    }
}
