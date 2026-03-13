package com.hr.service.impl;

import com.hr.service.AttachmentStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class LocalCacheAttachmentStorageService implements AttachmentStorageService {

    private static final String LOCAL_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @Override
    public StoredAttachment store(String businessType, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new RuntimeException("文件名格式无效");
        }

        String safeBusinessType = normalizeBusinessType(businessType);
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String generatedName = UUID.randomUUID().toString().replace("-", "") + extension;
        String relativeDir = safeBusinessType + "/";
        String absoluteDir = LOCAL_UPLOAD_DIR + relativeDir;

        File uploadDir = new File(absoluteDir);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new RuntimeException("无法创建上传目录: " + absoluteDir);
        }

        File target = new File(absoluteDir + generatedName);
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage(), e);
        }

        StoredAttachment storedAttachment = new StoredAttachment();
        storedAttachment.setFileName(originalFilename);
        storedAttachment.setFileKey("/" + relativeDir + generatedName);
        storedAttachment.setFileUrl("/uploads/" + relativeDir + generatedName);
        storedAttachment.setFileSize(file.getSize());
        return storedAttachment;
    }

    private String normalizeBusinessType(String businessType) {
        if (businessType == null || businessType.trim().isEmpty()) {
            return "common";
        }
        return businessType.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "_");
    }
}
