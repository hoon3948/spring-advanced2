package org.example.expert.domain.comment.controller;

import org.example.expert.domain.comment.service.CommentAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentAdminControllerTest {

    @Mock
    private CommentAdminService commentAdminService;

    @InjectMocks
    private CommentAdminController commentAdminController;

    @Test
    @DisplayName("댓글 삭제")
    void comment를_정상적으로_삭제한다() {

        long commentId = 100L;

        commentAdminController.deleteComment(commentId);

        verify(commentAdminService).deleteComment(commentId);
    }
}