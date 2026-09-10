package com.sandeep.userservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
       private String userId;
       private String email;
       private String firstName;
       private String lastName;
       private String tokenType="Bearer";

}
