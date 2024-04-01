package com.chatup.backend.services;

import com.mongodb.client.gridfs.model.GridFSFile;
import jakarta.annotation.Resource;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;

import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class ImageService {

    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations operations;

    public ImageService(GridFsTemplate gridFsTemplate, GridFsOperations operations) {
        this.gridFsTemplate = gridFsTemplate;
        this.operations = operations;
    }

    public String storeImage(MultipartFile file) throws IOException {
        ObjectId fileId = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType());
        return fileId.toString();
    }

    public Resource loadImage(String id) throws IOException {
        GridFSFile gridFsFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        if (gridFsFile == null) {
            throw new FileNotFoundException("File not found with id " + id);
        }

        GridFsResource resource = operations.getResource(gridFsFile);
        return (Resource) resource;
    }
}
