package com.chatup.backend.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
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

@Service
public class ImageService {
    private final GridFsTemplate gridFsTemplate;

    @Autowired
    public ImageService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    public String storeOrUpdateImage(String userId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }

        BufferedImage resizedImage = resizeImage(file);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpeg", os);
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

        gridFsTemplate.delete(new Query(Criteria.where("filename").is(userId)));

        DBObject metadata = new BasicDBObject();
        metadata.put("contentType", file.getContentType());

        ObjectId fileId = gridFsTemplate.store(is, userId, metadata);
        return fileId.toString();
    }

    private BufferedImage resizeImage(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        BufferedImage resizedImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        resizedImage.getGraphics().drawImage(originalImage, 0, 0, 300, 300, null);
        return resizedImage;
    }

    public Resource loadImage(String userId) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("filename").is(userId)));
        if (file == null) return null;
        return gridFsTemplate.getResource(file);
    }
}
