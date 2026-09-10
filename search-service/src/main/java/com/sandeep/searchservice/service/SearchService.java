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

    public List<UserDocument> searchBySkill(String skill) {

        return userSearchRepository.findBySkillsContaining(skill);

    }

    public List<PostDocument> searchPosts(String q) {

        log.info("Searching Posts: {}",q);
        return postSearchRepository.searchPosts(q);
    }
}
