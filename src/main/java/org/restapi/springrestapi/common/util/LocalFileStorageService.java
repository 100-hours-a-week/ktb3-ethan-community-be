package org.restapi.springrestapi.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.CommonErrorCode;
import org.restapi.springrestapi.exception.code.UploadErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.upload.base-dir}")
    private String baseDir;

    @Value("${app.upload.public-base-url}")
    private String publicBaseUrl;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg");

    @Override
    public String saveProfileImage(MultipartFile file) {
        String ext = validateAndGetExtension(file);

        String randomName = UUID.randomUUID().toString();
        String relativePath = Paths.get("profile", randomName + "." + ext).toString();

        return save(relativePath, file);
    }

    @Override
    public String savePostImage(MultipartFile file) {
        String ext = validateAndGetExtension(file);

        String randomName = UUID.randomUUID().toString();
        String relativePath = Paths.get("post", randomName + "." + ext).toString();

        return save(relativePath, file);
    }

    private String save(String relativePath, MultipartFile file) {
        Path dest = Paths.get(baseDir).resolve(relativePath).normalize();
        Path folder = dest.getParent();

        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            throw new AppException(CommonErrorCode.INTERNAL);
        }

        Path tmp = folder.resolve(dest.getFileName() + ".tmp");

        try (InputStream in = file.getInputStream()) {
            FileUtils.copyInputStreamToFile(in, tmp.toFile());
            try {
                Files.move(tmp, dest,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
            throw new AppException(CommonErrorCode.INTERNAL);
        }

        return toPublicUrl(relativePath);
    }

    private String toPublicUrl(String relativePath) {
        String base = publicBaseUrl.endsWith("/") ? publicBaseUrl : publicBaseUrl + "/";
        return base + relativePath.replace("\\", "/");
    }

    private String validateAndGetExtension(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(CommonErrorCode.BAD_REQUEST);
        }

        String originalName = Optional.ofNullable(file.getOriginalFilename())
                .orElseThrow(() -> new AppException(CommonErrorCode.BAD_REQUEST));

        String ext = getFileExtension(originalName);

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new AppException(UploadErrorCode.INVALID_FILE_TYPE);
        }
        return ext;
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            throw new AppException(UploadErrorCode.INVALID_FILE_TYPE);
        }
        return fileName.substring(lastDot + 1).toLowerCase();
    }
}