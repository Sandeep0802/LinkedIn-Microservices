package com.sandeep.searchservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDeletedEvent {

    private String postId;
    private String authorId;
}