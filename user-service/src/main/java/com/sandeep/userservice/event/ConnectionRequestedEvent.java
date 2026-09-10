package com.sandeep.userservice.event;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRequestedEvent {


    private String requesterId;
    private String receiverId;

}
