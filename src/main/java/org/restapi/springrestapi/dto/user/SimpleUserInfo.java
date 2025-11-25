package org.restapi.springrestapi.dto.user;

import lombok.Builder;

@Builder
public record SimpleUserInfo(
        String nickname
) {
    public static SimpleUserInfo unknown() {
        return SimpleUserInfo.builder()
                .nickname("unknown")
                .build();
    }
    public static SimpleUserInfo from(String nickname) {
        return SimpleUserInfo.builder()
                .nickname(nickname)
                .build();
    }
}
