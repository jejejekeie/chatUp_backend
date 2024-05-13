package com.chatup.backend.service;

import com.chatup.backend.models.Mensaje;
import com.chatup.backend.models.User;
import com.chatup.backend.repositories.MensajeRepository;
import com.chatup.backend.repositories.UserRepository;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MensajeService {
    private final MensajeRepository mensajeRepository;
    private final UserRepository userRepository;

    public MensajeService(MensajeRepository mensajeRepository, UserRepository userRepository) {
        this.mensajeRepository = mensajeRepository;
        this.userRepository = userRepository;
    }

    @CachePut(value = "messagesByChatId", key = "#mensaje.chatId")
    public Mensaje save(Mensaje mensaje) {
        if (mensaje.getTimestamp() == null) {
            mensaje.setTimestamp(new Date());
        }
        User user = userRepository.findById(mensaje.getSender())
                .orElseThrow(() -> new RuntimeException("User not found"));
        mensaje.setSenderUsername(user.getUsername());
        Mensaje savedMessage = mensajeRepository.save(mensaje);
        System.out.println("Saving message: " + savedMessage.getContent());
        return savedMessage;
    }

    public Page<Mensaje> findMensajesChatPageable(String chatId, int page, int size) {
        return mensajeRepository.findByChatIdOrderByTimestampDesc(chatId, PageRequest.of(page, size));
    }

    public List<Mensaje> findMensajesChat(String chatId) {
    List<Mensaje> messages = mensajeRepository.findByChatId(chatId);
    messages.forEach(msg -> {
        User user = userRepository.findById(msg.getSender()).orElse(null);
        if (user != null) {
            msg.setSenderUsername(user.getUsername());
        }
    });
    return messages;
    }

    @Cacheable(value = "messagesByChatId", key = "#id")
    public Mensaje findLastMessageByChatId(String id) {
        return mensajeRepository.findFirstByChatIdOrderByTimestampDesc(id);
    }
}
