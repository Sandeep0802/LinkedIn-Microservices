package com.sandeep.postservice.repository;

import com.sandeep.postservice.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post,String> {

     List<Post> findByAuthorIdOrderByCreatedAtDesc(String userId);
}
