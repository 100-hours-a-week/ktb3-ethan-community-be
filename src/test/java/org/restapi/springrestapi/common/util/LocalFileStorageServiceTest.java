package org.restapi.springrestapi.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.CommonErrorCode;
import org.restapi.springrestapi.exception.code.UploadErrorCode;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageServiceTest {

    LocalFileStorageService service;

    @TempDir
    Path baseDir;

    @BeforeEach
    void setUp() {
        service = new LocalFileStorageService();
        ReflectionTestUtils.setField(service, "baseDir", baseDir.toString());
        ReflectionTestUtils.setField(service, "publicBaseUrl", "http://localhost/files");
    }

    @Test
    @DisplayName("프로필 이미지를 저장하면 베이스 디렉터리에 파일이 생성되고 퍼블릭 URL을 반환한다")
    void saveProfileImage_createsFile() throws IOException {
        // given
        byte[] content = "fake image".getBytes();
        MockMultipartFile file =
                new MockMultipartFile("file", "image.jpg", "image/jpeg", content);

        // when
        String url = service.saveProfileImage(file);

        // then
        assertThat(url).startsWith("http://localhost/files/profile/");
        String relative = url.replace("http://localhost/files/", "");
        Path savedPath = baseDir.resolve(relative);
        assertThat(Files.exists(savedPath)).isTrue();
        assertThat(Files.readAllBytes(savedPath)).isEqualTo(content);
    }

    @Test
    @DisplayName("지원하지 않는 확장자는 AppException(UPLOAD.INVALID_FILE_TYPE)을 던진다")
    void savePostImage_invalidExtension_throws() {
        // given
        MockMultipartFile file =
                new MockMultipartFile("file", "image.gif", "image/gif", "gif".getBytes());

        // when & then
        assertThatThrownBy(() -> service.savePostImage(file))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(UploadErrorCode.INVALID_FILE_TYPE);
    }

    @Test
    @DisplayName("비어 있는 파일은 INVALID_REQUEST 예외를 던진다")
    void saveProfileImage_emptyFile_throws() {
        // given
        MockMultipartFile file =
                new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[0]);

        // when & then
        assertThatThrownBy(() -> service.saveProfileImage(file))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(CommonErrorCode.BAD_REQUEST);
    }
}
