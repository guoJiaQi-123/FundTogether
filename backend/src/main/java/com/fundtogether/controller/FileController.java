package com.fundtogether.controller;

import com.fundtogether.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传控制器
 * <p>
 * 提供文件上传功能，将用户上传的文件保存到服务器本地目录，
 * 并返回可访问的文件URL地址。
 * </p>
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    /** 文件上传存储目录，位于项目根目录下的uploads文件夹 */
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    /**
     * 上传文件
     * <p>
     * 接收MultipartFile文件，使用UUID重命名后保存到服务器本地uploads目录，
     * 返回文件的访问URL地址。
     * </p>
     *
     * @param file 上传的文件（MultipartFile类型）
     * @return 文件访问URL地址
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            // 确保上传目录存在，不存在则创建
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 获取原始文件名并提取扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // 使用UUID生成唯一文件名，避免文件名冲突
            String newFilename = UUID.randomUUID().toString() + extension;

            // 将文件保存到目标路径
            File dest = new File(UPLOAD_DIR + newFilename);
            file.transferTo(dest);

            // 返回文件的访问URL
            String fileUrl = "http://localhost:8080/uploads/" + newFilename;
            return Result.success(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败");
        }
    }
}
