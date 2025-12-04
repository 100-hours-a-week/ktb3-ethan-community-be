package org.restapi.springrestapi.dto.user;

import org.restapi.springrestapi.common.annotation.ValidNickname;

public record PatchProfileRequest(
        @ValidNickname
        String nickname,

        // nullable
        String profileImageUrl,

        boolean removeProfileImage
) {  }
