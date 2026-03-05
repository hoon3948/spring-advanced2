package org.example.expert.domain.todo.dto.response;

import lombok.Getter;

@Getter
public class TodoUpdateResponse {
    private final Long id;
    private final String title;
    private final String contents;

    public TodoUpdateResponse(Long id, String title, String contents) {
        this.id = id;
        this.title = title;
        this.contents = contents;
    }
}
