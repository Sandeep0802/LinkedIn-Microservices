package com.sandeep.postservice.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostCommentedEvent {

    private String postId;
    private String authorId;

    private String commentId;
    private String postAuthorId;
}
