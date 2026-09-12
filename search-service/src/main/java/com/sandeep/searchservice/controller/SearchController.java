package com.sandeep.searchservice.controller;


import com.sandeep.searchservice.entity.PostDocument;
import com.sandeep.searchservice.entity.UserDocument;
import com.sandeep.searchservice.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@Slf4j
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/people")
    public ResponseEntity<List<UserDocument>> searchUsers(@RequestParam String q){

         return ResponseEntity.ok(searchService.searchUsers(q));

    }

    @GetMapping("/people/all")
    public ResponseEntity<List<UserDocument>> getAllUsers() {

        return ResponseEntity.ok(
                searchService.getAllUsers()
        );
    }

    @GetMapping("/skills")
    public ResponseEntity<List<UserDocument>> searchBySkill(@RequestParam String skill){

        return ResponseEntity.ok(searchService.searchBySkill(skill));



    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostDocument>> searchPosts(@RequestParam String q){

        return ResponseEntity.ok(searchService.searchPosts(q));



    }
}
