package org.example.expert.domain.todo.controller;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.dto.response.TodoUpdateResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.expert.domain.user.enums.UserRole.USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoControllerTest {

    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoController todoController;

    @Test
    @DisplayName("todo를 정상적으로 등록")
    void todo를_정상적으로_등록한다() {

        AuthUser authUser =
                new AuthUser(1L, "test@test.com", USER);

        TodoSaveRequest request =
                new TodoSaveRequest("제목", "내용");

        TodoSaveResponse response =
                new TodoSaveResponse(
                        100L,
                        "제목",
                        "내용",
                        "맑음",
                        new UserResponse(1L, "test@test.com")
                );

        when(todoService.saveTodo(authUser, request))
                .thenReturn(response);

        ResponseEntity<TodoSaveResponse> result =
                todoController.saveTodo(authUser, request);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(100L, result.getBody().getId());
    }

    @Test
    @DisplayName("todo 목록를 정상적으로 조회")
    void todo_목록을_정상적으로_조회한다() {

        TodoResponse todoResponse =
                new TodoResponse(
                        10L,
                        "제목",
                        "내용",
                        "맑음",
                        new UserResponse(1L, "test@test.com"),
                        null,
                        null
                );

        Page<TodoResponse> page =
                new PageImpl<>(List.of(todoResponse));

        when(todoService.getTodos(1, 10))
                .thenReturn(page);

        ResponseEntity<Page<TodoResponse>> result =
                todoController.getTodos(1, 10);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(1, result.getBody().getTotalElements());
    }

    @Test
    @DisplayName("todo 단건 조회")
    void todo_단건을_정상적으로_조회한다() {

        TodoResponse response =
                new TodoResponse(
                        10L,
                        "제목",
                        "내용",
                        "맑음",
                        new UserResponse(1L, "test@test.com"),
                        null,
                        null
                );

        when(todoService.getTodo(10L))
                .thenReturn(response);

        ResponseEntity<TodoResponse> result =
                todoController.getTodo(10L);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals("제목", result.getBody().getTitle());
    }

    @Test
    @DisplayName("updateTodo 요청에 성공한다")
    void updateTodo에_성공한다() {

        // given
        long userId = 1L;
        long todoId = 10L;

        AuthUser authUser = new AuthUser(userId, "email@test.com", USER);

        TodoSaveRequest request = new TodoSaveRequest("title", "contents");

        TodoUpdateResponse response = new TodoUpdateResponse(todoId, "title", "contents");

        given(todoService.updateTodo(userId, todoId, request))
                .willReturn(response);

        // when
        ResponseEntity<TodoUpdateResponse> result =
                todoController.updateTodo(authUser, todoId, request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);

        verify(todoService).updateTodo(userId, todoId, request);
    }
}