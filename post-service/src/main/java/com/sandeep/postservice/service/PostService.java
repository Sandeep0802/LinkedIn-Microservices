package com.sandeep.postservice.service;

import com.sandeep.postservice.entity.Comment;
import com.sandeep.postservice.entity.Like;
import com.sandeep.postservice.entity.Post;
import com.sandeep.postservice.event.PostCommentedEvent;
import com.sandeep.postservice.event.PostCreatedEvent;
import com.sandeep.postservice.event.PostDeletedEvent;
import com.sandeep.postservice.event.PostLikedEvent;
import com.sandeep.postservice.repository.CommentRepository;
import com.sandeep.postservice.repository.LikeRepository;
import com.sandeep.postservice.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    private final S3Service s3Service;

    private final KafkaTemplate<String,Object> kafkaTemplate;

    private static final String POST_CREATED_TOPIC="post.created";
    private static final String POST_LIKED_TOPIC="post.liked";
    private static final String POST_COMMENTED_TOPIC="post.commented";
    private static final String POST_DELETED_TOPIC = "post.deleted";


    //create a post
    //optionally upload image to s3
    //Publish post.created event to kafka
    //Feed Service and Search Service will consume this
    public Post createPost(String authorId, String content, MultipartFile image) {

           log.info("Creating post for user: {}",authorId);

           Post post=new Post();
           post.setAuthorId(authorId);
           post.setContent(content);

           if(image!=null && !image.isEmpty()){

               String imageUrl= s3Service.uploadFile(image,"posts/"+authorId);

               post.setImageUrl(imageUrl);
           }

           Post savedPost=postRepository.save(post);

           log.info("Post Created: {}",savedPost.getId());

        PostCreatedEvent event=new PostCreatedEvent();

          event.setPostId(savedPost.getId());
          event.setAuthorId(savedPost.getAuthorId());
          event.setContent(savedPost.getContent());
          event.setImageUrl(savedPost.getImageUrl());
          event.setCreatedAt(savedPost.getCreatedAt().toString());

          kafkaTemplate.send(POST_CREATED_TOPIC,savedPost.getId(),event);

          log.info("Post created event published: {}",savedPost.getId());

          return savedPost;

    }

    public Post getPost(String postId) {

        return postRepository.findById(postId).orElseThrow(()->new RuntimeException(
                "Post not found: "+postId
        ));
    }

    public List<Post> getUserPosts(String userId) {

         return postRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
    }


    //like or unlike a post
    public  String likePost(String postId, String userId) {

         Post post=getPost(postId);

          //if already liked
          if(likeRepository.existsByPostIdAndUserId(postId,userId)){

              //unlike it
              likeRepository.findByPostIdAndUserId(postId,userId).ifPresent(likeRepository::delete);

              post.setLikeCount(post.getLikeCount()-1);
              postRepository.save(post);

              return "Post Unliked";
          }

          //Like
          Like like =new Like();
          like.setPostId(postId);
          like.setUserId(userId);
          likeRepository.save(like);
          post.setLikeCount(post.getLikeCount()+1);
          postRepository.save(post);

          //publish post.liked event

        PostLikedEvent event=new PostLikedEvent();
        event.setPostId(postId);
        event.setUserId(userId);
        event.setAuthorId(post.getAuthorId());

        kafkaTemplate.send(POST_LIKED_TOPIC,postId,event);

        log.info("Post liked event published: {}",post.getId());

        return "Post Liked";
    }



    public Comment addComment(String postId, String authorId, String content) {

        Post post=getPost(postId);
        Comment comment=new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(authorId);
        comment.setContent(content);

        Comment savedComment=commentRepository.save(comment);

        post.setCommentCount(post.getCommentCount()+1);

        postRepository.save(post);

        //publish post.commented event
        PostCommentedEvent event=new PostCommentedEvent();

        event.setPostId(postId);
        event.setAuthorId(authorId);
        event.setPostAuthorId(post.getAuthorId());
        event.setCommentId(savedComment.getId());

        kafkaTemplate.send(POST_COMMENTED_TOPIC,postId,event);

        log.info("Post commented event published: {}",post.getId());

        return savedComment;
    }

    public void deletePost(String postId, String userId) {

        Post post = getPost(postId);

        if (!post.getAuthorId().equals(userId)) {
            throw new RuntimeException("Not authorize to delete this post");
        }

        String authorId = post.getAuthorId();

        postRepository.delete(post);

        log.info("Post Deleted: {}", postId);

        PostDeletedEvent event = new PostDeletedEvent();
        event.setPostId(postId);
        event.setAuthorId(authorId);

        kafkaTemplate.send(
                POST_DELETED_TOPIC,
                postId,
                event
        );

        log.info(
                "Post deleted event published: {}",
                postId
        );
    }

    public  List<Comment> getComments(String postId) {

         return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
       }
}
