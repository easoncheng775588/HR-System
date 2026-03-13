package com.hr.service;

import org.springframework.web.multipart.MultipartFile;

public interface AttachmentStorageService {
    StoredAttachment store(String businessType, MultipartFile file);

    class StoredAttachment {
        private String fileName;
        private String fileUrl;
        private String fileKey;
        private long fileSize;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public String getFileKey() {
            return fileKey;
        }

        public void setFileKey(String fileKey) {
            this.fileKey = fileKey;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }
    }
}
