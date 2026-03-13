package com.hr.controller;

import com.hr.service.AttachmentStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FileUploadController {

    // 上传文件的保存路径
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    private final Path uploadPath = Paths.get(UPLOAD_DIR);

    private final AttachmentStorageService attachmentStorageService;

    public FileUploadController(AttachmentStorageService attachmentStorageService) {
        this.attachmentStorageService = attachmentStorageService;
    }

    @PostMapping("/upload")
    public Map<String, Object> uploadFile(@RequestParam(value = "resume", required = false) MultipartFile resumeFile,
                                          @RequestParam(value = "file", required = false) MultipartFile plainFile,
                                          @RequestParam(value = "businessType", required = false) String businessType) {
        Map<String, Object> response = new HashMap<>();
        MultipartFile file = resumeFile != null ? resumeFile : plainFile;
        try {
            AttachmentStorageService.StoredAttachment stored = attachmentStorageService.store(businessType, file);

            // 返回上传成功的信息
            Map<String, Object> body = new HashMap<>();
            body.put("url", stored.getFileUrl());
            body.put("name", stored.getFileName());
            body.put("size", stored.getFileSize());
            body.put("fileKey", stored.getFileKey());

            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", body);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "上传失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 静态资源处理器，用于访问上传的文件
     * @param request http请求
     * @return 文件资源
     */
    @GetMapping("/uploads/**")
    @ResponseBody
    public Resource serveFile(javax.servlet.http.HttpServletRequest request) {
        try {
            String uri = request.getRequestURI();
            String prefix = "/api/uploads/";
            String relativePath = uri.substring(uri.indexOf(prefix) + prefix.length());
            if (relativePath.trim().isEmpty()) {
                throw new RuntimeException("文件路径不能为空");
            }
            Path file = uploadPath.resolve(relativePath);
            Resource resource = new UrlResource(Objects.requireNonNull(file.toUri()));
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + relativePath);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file", e);
        }
    }
}
