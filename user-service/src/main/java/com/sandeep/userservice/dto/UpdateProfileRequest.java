package com.sandeep.userservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {

    private String firstName;
    private String lastName;
    private String headline;
    private String about;
    private String location;
    private List<String> skills;
}
