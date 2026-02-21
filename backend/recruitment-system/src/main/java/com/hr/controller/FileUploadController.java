package com.hr.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FileUploadController {

    // 上传文件的保存路径
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    private final Path uploadPath = Paths.get(UPLOAD_DIR);

    @PostMapping("/upload")
    public Map<String, Object> uploadFile(@RequestParam("resume") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                response.put("returnCode", "ERR0000");
                response.put("errorMsg", "文件为空");
                response.put("body", null);
                return response;
            }

            // 创建上传目录（如果不存在）
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                System.out.println("Upload directory created: " + created);
                System.out.println("Upload directory path: " + uploadDir.getAbsolutePath());
            }
            if (!uploadDir.exists()) {
                response.put("returnCode", "ERR0000");
                response.put("errorMsg", "无法创建上传目录: " + UPLOAD_DIR);
                response.put("body", null);
                return response;
            }

            // 生成唯一的文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + fileExtension;
            String filePath = UPLOAD_DIR + fileName;

            // 保存文件
            File dest = new File(filePath);
            file.transferTo(dest);

            // 返回上传成功的信息
            Map<String, Object> body = new HashMap<>();
            body.put("url", "/uploads/" + fileName);
            body.put("name", originalFilename);
            body.put("size", file.getSize());

            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", body);
        } catch (IOException e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "文件上传失败：" + e.getMessage());
            response.put("body", null);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "上传失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 静态资源处理器，用于访问上传的文件
     * @param filename 文件名
     * @return 文件资源
     */
    @GetMapping("/uploads/{filename:.+}")
    @ResponseBody
    public Resource serveFile(@PathVariable String filename) {
        try {
            Path file = uploadPath.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }
}
