package com.sandeep.notificationservice.event;


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
