package com.sandeep.searchservice.service;

import com.sandeep.searchservice.entity.PostDocument;
import com.sandeep.searchservice.entity.UserDocument;
import com.sandeep.searchservice.repository.PostSearchRepository;
import com.sandeep.searchservice.repository.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

    private final PostSearchRepository postSearchRepository;
    private final UserSearchRepository userSearchRepository;


    public List<UserDocument> searchUsers(String q) {

        log.info("Searching Users: {}",q);

        return userSearchRepository.searchUsers(q);

    }
    public List<UserDocument> getAllUsers() {

        log.info("Fetching all users from search index");

        return StreamSupport
                .stream(
                        userSearchRepository.findAll().spliterator(),
                        false
                )
                .toList();
    }
    public List<UserDocument> searchBySkill(String skill) {

        log.info("Searching Skills: {}", skill);

        return userSearchRepository.searchBySkill(skill);

    }

    public List<PostDocument> searchPosts(String q) {

        log.info("Searching Posts: {}",q);
        return postSearchRepository.searchPosts(q);
    }
}
