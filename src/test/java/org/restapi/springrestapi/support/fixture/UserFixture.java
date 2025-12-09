package org.restapi.springrestapi.support.fixture;

import org.restapi.springrestapi.model.User;

public final class UserFixture {
    private UserFixture() {}

    public static User persistedUser() {
        return User.builder()
            .nickname("nickname")
            .email("user@user.com")
            .password("Password1!") // should be encoded before persisting
            .profileImageUrl("http://img")
            .build();
    }

    public static User persistedUser(Long id) {
        return persistedUser().toBuilder()
            .id(id)
            .build();
    }

    public static User uniqueUser(String suffix) {
        long now = System.nanoTime();
        String token = Long.toString(now, 36); // compact alphanumeric
        if (token.length() > 9) {
            token = token.substring(token.length() - 9);
        }
        String nickname = ("u" + token);
        if (nickname.length() > 10) {
            nickname = nickname.substring(0, 10);
        } else if (nickname.length() < 2) {
            nickname = "u1";
        }

        return persistedUser().toBuilder()
            .email("user_" + suffix + "_" + now + "@test.com")
            .nickname(nickname)
            .build();
    }
}
