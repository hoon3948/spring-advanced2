package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;

    @Test
    public void manager_목록_조회_시_Todo가_없다면_NPE_에러를_던진다() {
        // given
        long todoId = 1L;
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    void todo의_user가_null인_경우_예외가_발생한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", null);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("일정을 생성한 유저만 담당자를 지정할 수 있습니다.", exception.getMessage());
    }

    @Test // 테스트코드 샘플
    public void manager_목록_조회에_성공한다() {
        // given
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
    }

    @Test // 테스트코드 샘플
    void todo가_정상적으로_등록된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
        given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

        // then
        assertNotNull(response);
        assertEquals(managerUser.getId(), response.getUser().getId());
        assertEquals(managerUser.getEmail(), response.getUser().getEmail());
    }


    @Test
    @DisplayName("등록하려는 관리자가 존재하지 않으면 예외 발생")
    void manager가_존재하지_않으면_예외_발생() {

        AuthUser authUser = new AuthUser(1L, "a@test.com", UserRole.USER);
        ManagerSaveRequest request = new ManagerSaveRequest(99L);

        User creator = new User("a@test.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(creator, "id", 1L);

        Todo todo = mock(Todo.class);
        when(todo.getUser()).thenReturn(creator);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class,
                () -> managerService.saveManager(authUser, 1L, request));
    }

    @Test
    @DisplayName("작성자는 본인을 담당자로 등록할수없다.")
    void manager로_자신을_등록할_수_없다() {

        AuthUser authUser = new AuthUser(1L, "a@test.com", UserRole.USER);
        ManagerSaveRequest request = new ManagerSaveRequest(1L);

        User creator = new User("a@test.com", "pw", UserRole.USER);
        ReflectionTestUtils.setField(creator, "id", 1L);

        Todo todo = mock(Todo.class);
        when(todo.getUser()).thenReturn(creator);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));

        assertThrows(InvalidRequestException.class,
                () -> managerService.saveManager(authUser, 1L, request));
    }

    @Test
    @DisplayName("삭제시 유저가 없으면 예외 발생")
    void delete시_유저가_없으면_예외() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(1L, 1L, 1L));
    }

    @Test
    @DisplayName("삭제시 todo가 없으면 예외 발생")
    void delete시_todo가_없으면_예외() {

        User user = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(todoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(1L, 1L, 1L));
    }

    @Test
    @DisplayName("삭제시 일정 작성자가 아니면 예외 발생")
    void delete시_일정작성자가_아니면_예외() {

        User loginUser = mock(User.class);
        when(loginUser.getId()).thenReturn(1L);

        User todoUser = mock(User.class);
        when(todoUser.getId()).thenReturn(2L);

        Todo todo = mock(Todo.class);
        when(todo.getUser()).thenReturn(todoUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(loginUser));
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));

        assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(1L, 1L, 1L));
    }

    @Test
    @DisplayName("삭제시 manager가 없으면 예외 발생")
    void delete시_manager가_없으면_예외() {

        User loginUser = mock(User.class);
        when(loginUser.getId()).thenReturn(1L);

        Todo todo = mock(Todo.class);
        when(todo.getUser()).thenReturn(loginUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(loginUser));
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(managerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(1L, 1L, 1L));
    }

    @Test
    @DisplayName("삭제시 다른 todo의 manager면 예외 발생")
    void delete시_다른_todo의_manager면_예외() {

        User loginUser = mock(User.class);
        when(loginUser.getId()).thenReturn(1L);

        Todo todo = mock(Todo.class);
        when(todo.getId()).thenReturn(1L);
        when(todo.getUser()).thenReturn(loginUser);

        Todo otherTodo = mock(Todo.class);
        when(otherTodo.getId()).thenReturn(2L);

        Manager manager = mock(Manager.class);
        when(manager.getTodo()).thenReturn(otherTodo);

        when(userRepository.findById(1L)).thenReturn(Optional.of(loginUser));
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(managerRepository.findById(1L)).thenReturn(Optional.of(manager));

        assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(1L, 1L, 1L));
    }

    @Test
    @DisplayName("매니저를 정상적으로 삭제")
    void manager를_정상적으로_삭제() {

        User loginUser = mock(User.class);
        when(loginUser.getId()).thenReturn(1L);

        Todo todo = mock(Todo.class);
        when(todo.getId()).thenReturn(1L);
        when(todo.getUser()).thenReturn(loginUser);

        Manager manager = mock(Manager.class);
        when(manager.getTodo()).thenReturn(todo);

        when(userRepository.findById(1L)).thenReturn(Optional.of(loginUser));
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(managerRepository.findById(1L)).thenReturn(Optional.of(manager));

        managerService.deleteManager(1L, 1L, 1L);

        verify(managerRepository).delete(manager);
    }
}
