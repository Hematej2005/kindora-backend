package com.kindora.kindora_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class FileStorageService {

    private final Cloudinary cloudinary;

    public FileStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String store(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) return null;

        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "kindora/donations",
                        "resource_type", "image"
                )
        );

        return result.get("secure_url").toString(); // ✅ CLOUD URL
    }
}
