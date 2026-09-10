package com.sandeep.userservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {

    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String headline;
    private String location;
}
