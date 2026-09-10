package com.sandeep.feedservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "user-service",
        url = "${user.service.url}"
)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{userId}/connections")
    List<Map<String, Object>> getConnections(
            @PathVariable String userId
    );
}