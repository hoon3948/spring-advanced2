package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private TodoService todoService;

    @Test
    @DisplayName("todo를 정상적으로 저장")
    void todo를_정상적으로_저장한다() {

        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
        TodoSaveRequest request = new TodoSaveRequest("제목", "내용");

        when(weatherClient.getTodayWeather()).thenReturn("맑음");

        Todo savedTodo = new Todo("제목", "내용", "맑음",
                User.fromAuthUser(authUser));
        ReflectionTestUtils.setField(savedTodo, "id", 100L);

        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);

        TodoSaveResponse response =
                todoService.saveTodo(authUser, request);

        assertEquals(100L, response.getId());
        assertEquals("제목", response.getTitle());
        assertEquals("내용", response.getContents());
        assertEquals("맑음", response.getWeather());
        assertEquals(1L, response.getUser().getId());
    }

    @Test
    @DisplayName("todo 목록을 정상적으로 조회")
    void todo_목록을_정상적으로_조회한다() {

        User user = new User("test@test.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Todo todo = new Todo("제목", "내용", "맑음", user);
        ReflectionTestUtils.setField(todo, "id", 10L);

        Page<Todo> page =
                new PageImpl<>(List.of(todo));

        when(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class)))
                .thenReturn(page);

        Page<TodoResponse> result =
                todoService.getTodos(1, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(10L, result.getContent().get(0).getId());
    }

    @Test
    @DisplayName("todo를 정상적으로 단건 조회")
    void todo_단건을_정상적으로_조회한다() {

        User user = new User("test@test.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Todo todo = new Todo("제목", "내용", "맑음", user);
        ReflectionTestUtils.setField(todo, "id", 10L);

        when(todoRepository.findByIdWithUser(10L))
                .thenReturn(Optional.of(todo));

        TodoResponse response = todoService.getTodo(10L);

        assertEquals(10L, response.getId());
        assertEquals("제목", response.getTitle());
    }

    @Test
    @DisplayName("없는 todo 단건 조회시 예외 발생")
    void todo_단건조회시_존재하지_않으면_예외_발생() {

        when(todoRepository.findByIdWithUser(1L))
                .thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class,
                () -> todoService.getTodo(1L));
    }
}