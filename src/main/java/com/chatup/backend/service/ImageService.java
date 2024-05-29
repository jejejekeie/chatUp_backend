package com.chatup.backend.service;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class ImageService {
    private final GridFsTemplate gridFsTemplate;

    public ImageService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    public String storeImage(String userId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = userId + "_profile" + extension;
        ObjectId fileId = gridFsTemplate.store(file.getInputStream(), filename, file.getContentType());
        return fileId.toString();
    }

    public Resource loadImage(String fileId) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(fileId)));
        if (file == null) return null;
        return gridFsTemplate.getResource(file);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    public boolean isValidFile(MultipartFile file) {
        String contentType = file.getContentType();
        long size = file.getSize();
        return (contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png")) && size <= 200_000_000);
    }
}