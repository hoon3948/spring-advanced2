package org.example.expert.domain.comment.entity;

import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Comment 엔티티 테스트")
class CommentTest {

    @Test
    @DisplayName("update에 성공한다")
    void update에_성공한다() {

        // given
        User user = new User("email@test.com", "password", UserRole.USER);
        Comment comment = new Comment("기존 댓글", user, null);

        // when
        comment.update("수정된 댓글");

        // then
        assertThat(comment.getContents()).isEqualTo("수정된 댓글");
    }
}