package org.restapi.springrestapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.restapi.springrestapi.config.PasswordEncoder;
import org.restapi.springrestapi.dto.user.PatchProfileRequest;
import org.restapi.springrestapi.dto.user.RegisterUserRequest;

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
@SQLDelete(sql = "UPDATE `user` SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

	private String profileImageUrl; // nullable

    @Column(nullable = false)
    private LocalDateTime joinAt;

    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "author")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Comment> comments = new ArrayList<>();

    /*
    constructor
    - from(RegisterUserRequest, PasswordEncoder)
     */

    public static User from(
		RegisterUserRequest command,
		PasswordEncoder passwordEncoder
	) {
		return User.builder()
                .nickname(command.getNickname())
                .email(command.getEmail())
                .password(passwordEncoder.encode(command.getPassword()))
                .profileImageUrl(command.getProfileImageUrl())
                .joinAt(LocalDateTime.now())
                .build();
	}

    /*
    setter
    - updateProfile(PatchProfileRequest)
    - updatePassword(String, PasswordEncoder)
     */
	public void updateProfile(PatchProfileRequest command) {
		this.nickname = command.getNickname();
		if (command.getProfileImageUrl() != null) {
			this.profileImageUrl = command.getProfileImageUrl();
		}
	}

	public void updatePassword(String password, PasswordEncoder passwordEncoder) {
		this.password = passwordEncoder.encode(password);
	}

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
