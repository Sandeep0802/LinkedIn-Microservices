package com.sandeep.userservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionAcceptedEvent {

    private String requesterId;
    private String receiverId;
}
