package com.studystation.controller;

import com.studystation.pojo.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class UploadController {

    // 读取 yml 里的配置路径
    @Value("${studystation.upload.path}")
    private String uploadPath;

    @PostMapping("/avatar")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("上传的文件不能为空");
        }

        // 1. 获取原始文件名和后缀
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 2. 生成唯一的新文件名（UUID + 后缀）
        String newFileName = UUID.randomUUID().toString() + extension;

        // 3. 检查目录是否存在，不存在则创建
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 4. 保存文件
        File dest = new File(uploadPath + newFileName);
        try {
            file.transferTo(dest);
            // 5. 返回可以直接访问的 URL（前端加上后端地址即可）
            // 返回格式如：/images/123e4567-e89b-12d3-a456-426614174000.jpg
            return Result.success("/images/" + newFileName);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件保存失败");
        }
    }
}