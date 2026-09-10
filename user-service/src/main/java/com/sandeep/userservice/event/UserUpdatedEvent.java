package com.sandeep.userservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {

    private String userId;
    private String firstName;
    private String lastName;
    private String headline;
    private String about;
    private String location;
    private List<String> skills;
}