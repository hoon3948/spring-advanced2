package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.expert.domain.user.enums.UserRole.USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;

    @Test
    public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", USER);

        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, todoId, request);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void comment를_정상적으로_등록한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(result);
    }

    @Test
    @DisplayName("댓글 없음")
    void comment가_없으면_빈_리스트를_반환한다() {

        when(commentRepository.findByTodoIdWithUser(1L))
                .thenReturn(Collections.emptyList());

        List<CommentResponse> result = commentService.getComments(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("댓글 여러개")
    void comment_목록을_정상적으로_반환한다() {

        // 유저 생성
        User user1 = new User("user1@test.com", "pw", USER);
        ReflectionTestUtils.setField(user1, "id", 1L);

        User user2 = new User("user2@test.com", "pw", USER);
        ReflectionTestUtils.setField(user2, "id", 2L);

        // Todo는 실제 로직 사용 안하므로 mock
        Todo todo = mock(Todo.class);

        // 댓글 생성
        Comment comment1 = new Comment("댓글1", user1, todo);
        ReflectionTestUtils.setField(comment1, "id", 10L);

        Comment comment2 = new Comment("댓글2", user2, todo);
        ReflectionTestUtils.setField(comment2, "id", 20L);

        when(commentRepository.findByTodoIdWithUser(1L))
                .thenReturn(List.of(comment1, comment2));

        List<CommentResponse> result = commentService.getComments(1L);

        assertEquals(2, result.size());

        // 첫 번째 댓글 검증
        assertEquals(10L, result.get(0).getId());
        assertEquals("댓글1", result.get(0).getContents());
        assertEquals("user1@test.com", result.get(0).getUser().getEmail());

        // 두 번째 댓글 검증
        assertEquals(20L, result.get(1).getId());
        assertEquals("댓글2", result.get(1).getContents());
        assertEquals("user2@test.com", result.get(1).getUser().getEmail());
    }

    @Test
    @DisplayName("댓글 업데이트 성공")
    void updateComment에_성공한다() {

        // given
        long userId = 1L;
        long commentId = 10L;

        AuthUser authUser = new AuthUser(userId, "email@test.com", USER);

        CommentSaveRequest request = new CommentSaveRequest("수정된 댓글");

        User user = User.fromAuthUser(authUser);

        Comment comment = mock(Comment.class);
        User commentUser = mock(User.class);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(comment.getUser()).willReturn(commentUser);
        given(commentUser.getId()).willReturn(userId);

        // when
        commentService.updateComment(authUser, commentId, request);

        // then
        verify(comment).update(request.getContents());
    }

    @Test
    @DisplayName("댓글 업데이트 시 댓글이 존재하지 않으면 실패한다")
    void updateComment_댓글이_없으면_실패한다() {

        // given
        long commentId = 10L;
        AuthUser authUser = new AuthUser(1L, "email@test.com", USER);

        CommentSaveRequest request = new CommentSaveRequest("댓글");

        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(authUser, commentId, request)
        ).isInstanceOf(InvalidRequestException.class)
                .hasMessage("Comment not found");
    }

    @Test
    @DisplayName("댓글 업데이트 시 본인의 댓글이 아니면 실패한다")
    void updateComment_본인댓글이_아니면_실패한다() {

        // given
        long userId = 1L;
        long commentId = 10L;

        AuthUser authUser = new AuthUser(userId, "email@test.com", USER);

        CommentSaveRequest request = new CommentSaveRequest("수정");

        Comment comment = mock(Comment.class);
        User commentUser = mock(User.class);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(comment.getUser()).willReturn(commentUser);
        given(commentUser.getId()).willReturn(2L);

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(authUser, commentId, request)
        ).isInstanceOf(InvalidRequestException.class)
                .hasMessage("본인의 댓글만 수정할 수 있습니다");
    }
}
