package com.chatup.backend.repositories;

import com.chatup.backend.models.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MensajeRepository extends MongoRepository<Mensaje, String> {
    List<Mensaje> findByChatId(String chatId);
    Page<Mensaje> findByChatIdOrderByTimestampDesc(String chatId, Pageable pageable);
    Mensaje findFirstByChatIdOrderByTimestampDesc(String id);
}
