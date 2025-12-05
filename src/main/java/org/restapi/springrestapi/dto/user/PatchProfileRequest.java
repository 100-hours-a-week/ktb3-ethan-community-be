package org.restapi.springrestapi.dto.user;

import org.restapi.springrestapi.common.annotation.ValidNickname;

public record PatchProfileRequest(
        @ValidNickname
        String nickname,

        String profileImageUrl,

        boolean removeProfileImage
) {  }
