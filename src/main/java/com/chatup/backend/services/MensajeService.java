package com.chatup.backend.services;

import com.chatup.backend.models.Mensaje;
import com.chatup.backend.repositories.MensajeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class MensajeService {
    private final MensajeRepository mensajeRepository;
    private final ChatService chatService;

    public MensajeService(MensajeRepository mensajeRepository, ChatService chatService) {
        this.mensajeRepository = mensajeRepository;
        this.chatService = chatService;
    }

    public Mensaje save(Mensaje mensaje) {
        assert mensaje.getSender() != null;
        Set<String> members = new java.util.HashSet<>(Set.of(mensaje.getSender()));

        members.add(mensaje.getSender());

        var chatId = chatService.getChatId(members, true).orElseThrow(() -> new RuntimeException("Chat not found"));
        mensaje.setChatId(chatId);
        mensajeRepository.save(mensaje);
        return mensaje;
    }

    public Page<Mensaje> findMensajesChatPageable(String chatId, int page, int size) {
        return mensajeRepository.findByChatIdOrderByTimestampDesc(chatId, PageRequest.of(page, size));
    }

    public List<Mensaje> findMensajesChat(String chatId) {
        return mensajeRepository.findByChatId(chatId);
    }

    public Mensaje findLastMessageByChatId(String id) {
        return mensajeRepository.findFirstByChatIdOrderByTimestampDesc(id);
    }
}
