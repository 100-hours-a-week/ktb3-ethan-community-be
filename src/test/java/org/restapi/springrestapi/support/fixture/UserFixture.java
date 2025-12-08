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
}
