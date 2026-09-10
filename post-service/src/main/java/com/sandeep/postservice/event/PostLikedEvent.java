package com.sandeep.postservice.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostLikedEvent {

    private String postId;
    private String userId;
    private String authorId;

}
