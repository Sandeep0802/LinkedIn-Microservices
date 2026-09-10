package com.sandeep.feedservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostCreatedEvent {

    private String postId;
    private String authorId;
    private String content;

    private String imageUrl;
    private String createdAt;
}

