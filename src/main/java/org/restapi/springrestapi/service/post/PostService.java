package org.restapi.springrestapi.service.post;

import jakarta.servlet.http.HttpServletRequest;
import org.restapi.springrestapi.dto.post.PatchPostRequest;
import org.restapi.springrestapi.dto.post.PostListResult;
import org.restapi.springrestapi.dto.post.PostResult;
import org.restapi.springrestapi.dto.post.RegisterPostRequest;
import org.restapi.springrestapi.dto.post.PostSummary;

public interface PostService {
	PostSummary createPost(Long userId, RegisterPostRequest command);
	PostListResult getPostList(Long cursor, int limit);
	PostResult getPost(HttpServletRequest request, Long userId, Long id);
    PostResult updatePost(Long userId, Long id, PatchPostRequest request);
	void deletePost(Long userId, Long postId);
}
