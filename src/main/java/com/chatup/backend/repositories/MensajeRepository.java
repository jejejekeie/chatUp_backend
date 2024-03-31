package com.chatup.backend.repositories;

import com.chatup.backend.models.Mensaje;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MensajeRepository extends MongoRepository<Mensaje, String> {
    List<Mensaje> findByChatId(String chatId);
}
