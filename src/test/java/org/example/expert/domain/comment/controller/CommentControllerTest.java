package org.example.expert.domain.comment.controller;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    @Test
    @DisplayName("댓글 저장")
    void comment를_정상적으로_저장한다() {

        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
        long todoId = 10L;
        CommentSaveRequest request = new CommentSaveRequest("댓글 내용");

        CommentSaveResponse response =
                new CommentSaveResponse(
                        100L,
                        "댓글 내용",
                        new UserResponse(1L, "test@test.com")
                );

        when(commentService.saveComment(authUser, todoId, request))
                .thenReturn(response);

        ResponseEntity<CommentSaveResponse> result =
                commentController.saveComment(authUser, todoId, request);

        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals(100L, result.getBody().getId());
        assertEquals("댓글 내용", result.getBody().getContents());
        assertEquals("test@test.com", result.getBody().getUser().getEmail());
    }

    @Test
    @DisplayName("댓글 조회")
    void comment_목록을_정상적으로_조회한다() {

        long todoId = 10L;

        CommentResponse comment1 =
                new CommentResponse(
                        1L,
                        "댓글1",
                        new UserResponse(1L, "user1@test.com")
                );

        CommentResponse comment2 =
                new CommentResponse(
                        2L,
                        "댓글2",
                        new UserResponse(2L, "user2@test.com")
                );

        when(commentService.getComments(todoId))
                .thenReturn(List.of(comment1, comment2));

        ResponseEntity<List<CommentResponse>> result =
                commentController.getComments(todoId);

        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());

        assertEquals("댓글1", result.getBody().get(0).getContents());
        assertEquals("user2@test.com", result.getBody().get(1).getUser().getEmail());
    }
}