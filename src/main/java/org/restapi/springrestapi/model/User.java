package org.restapi.springrestapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import org.restapi.springrestapi.common.annotation.ValidEmail;
import org.restapi.springrestapi.common.annotation.ValidNickname;
import org.restapi.springrestapi.dto.auth.SignUpRequest;
import org.restapi.springrestapi.dto.user.EncodedPassword;
import org.restapi.springrestapi.dto.user.PatchProfileRequest;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(name="users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Column(nullable = false)
    @ValidNickname
    private String nickname;

    @Column(nullable = false)
    @ValidEmail
    private String email;

    @Column(nullable = false)
    @NotNull
    private String password;

	private String profileImageUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinAt;

    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "author")
    @Builder.Default
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    /*
    constructor
    - from(RegisterUserRequest, PasswordEncoder)
     */

    @PrePersist
    private void prePersist() {
        this.joinAt = LocalDateTime.now();
    }

    public static User from(
        SignUpRequest signUpRequest,
        EncodedPassword password
	) {
		return User.builder()
                .email(signUpRequest.email())
                .password(password.value())
                .nickname(signUpRequest.nickname())
                .profileImageUrl(signUpRequest.profileImageUrl())
                .build();
	}

    /*
    setter
    - updateProfile(PatchProfileRequest)
    - updatePassword(String, PasswordEncoder)
     */
    public void updateProfile(PatchProfileRequest request) {
        this.nickname = request.nickname();

        if (request.removeProfileImage()) {
            this.profileImageUrl = null;
        } else if (request.profileImageUrl() != null){
            this.profileImageUrl = request.profileImageUrl();
        }
    }

    public void updatePassword(EncodedPassword password) {
        this.password = password.value();
    }
}
