package com.sandeep.postservice.repository;


import com.sandeep.postservice.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,String> {

    List<Comment> findByPostIdOrderByCreatedAtDesc(String postId);
}
