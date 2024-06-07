package com.chatup.backend.service;

import com.chatup.backend.repositories.UserRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import com.chatup.backend.models.User;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

@Service
public class ImageService {
    private final GridFsTemplate gridFsTemplate;
    private final UserRepository userRepository;

    @Autowired
    public ImageService(GridFsTemplate gridFsTemplate, UserRepository userRepository) {
        this.gridFsTemplate = gridFsTemplate;
        this.userRepository = userRepository;
    }

    public String storeOrUpdateImage(String userId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }

        BufferedImage resizedImage = resizeImage(file);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpeg", os);
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

        gridFsTemplate.delete(new Query(Criteria.where("metadata.userId").is(userId)));

        String extension = Objects.requireNonNull(file.getContentType()).split("/")[1];
        String filename = userId + "." + extension;

        DBObject metadata = new BasicDBObject();
        metadata.put("userId", userId);
        metadata.put("contentType", file.getContentType());

        ObjectId fileId = gridFsTemplate.store(is, filename, metadata);

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setFotoPerfil("/api/configuration/image/" + userId);
        userRepository.save(user);

        return fileId.toString();
    }

    private BufferedImage resizeImage(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int newWidth = 300;
        int newHeight = (height * newWidth) / width;

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        resizedImage.getGraphics().drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        return resizedImage;
    }

    public Resource loadImage(String userId) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("metadata.userId").is(userId)));
        if (file == null) return null;
        return gridFsTemplate.getResource(file);
    }
}
