package org.restapi.springrestapi.common.util;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String saveProfileImage(MultipartFile file);
    String savePostImage(MultipartFile file);
}