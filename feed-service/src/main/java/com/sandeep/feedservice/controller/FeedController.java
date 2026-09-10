package com.sandeep.feedservice.controller;


import com.sandeep.feedservice.service.FeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feed")
@Slf4j
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;


    //get paginated feed for user
    //return list of post Id's
    //client fetch full post details from Post Service
     @GetMapping("/{userId}")
     public ResponseEntity<List<String>> getFeed(@PathVariable String userId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size){


         return ResponseEntity.ok(feedService.getFeed(userId,page,size));

     }

    @DeleteMapping("/{userId}/cache")
    public ResponseEntity<String> getFeed(@PathVariable String userId){

        feedService.clearFeed(userId);
        return ResponseEntity.ok("Feed Cache cleared");

    }


}
