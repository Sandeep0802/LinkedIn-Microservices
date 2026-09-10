package com.sandeep.searchservice.service;


import com.sandeep.searchservice.entity.PostDocument;
import com.sandeep.searchservice.entity.UserDocument;
import com.sandeep.searchservice.event.PostCreatedEvent;
import com.sandeep.searchservice.event.UserCreatedEvent;
import com.sandeep.searchservice.event.UserUpdatedEvent;
import com.sandeep.searchservice.repository.PostSearchRepository;
import com.sandeep.searchservice.repository.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchEventConsumer {

    private final UserSearchRepository userSearchRepository;
    private final PostSearchRepository postSearchRepository;

    @KafkaListener(topics = "user.created")
    public void consumeUserCreatedEvent(UserCreatedEvent event) {

        try {

            log.info("Indexing new User: {}", event.getUserId());

            UserDocument document = new UserDocument();
            document.setId(event.getUserId());
            document.setFirstName(event.getFirstName());
            document.setLastName(event.getLastName());
            document.setEmail(event.getEmail());
            document.setHeadline(event.getHeadline());
            document.setLocation(event.getLocation());

            userSearchRepository.save(document);
            log.info("User indexed: {}", event.getUserId());
        } catch (Exception e) {

            log.error("Error indexing user: {}", event.getUserId());

        }


    }


    @KafkaListener(topics = "user.updated")
    public void consumeUserUpdatedEvent(UserUpdatedEvent event) {

        try {

            String userId = event.getUserId();
            log.info("Updating user index: {}", userId);

            userSearchRepository.findById(userId).ifPresent(doc -> {
                doc.setFirstName(event.getFirstName());
                doc.setLastName(event.getLastName());
                doc.setHeadline(event.getHeadline());
                doc.setLocation(event.getLocation());

                if (event.getSkills() != null) {
                    doc.setSkills(event.getSkills());
                }

                userSearchRepository.save(doc);
                log.info("User index updated: {}", userId);

            });

        } catch (Exception e) {

            log.error("Error indexing user: {}", event.getUserId());

        }

    }

    @KafkaListener(topics = "post.created")
    public void consumePostCreatedEvent(PostCreatedEvent event) {

        try {

            log.info("Indexing new Post: {}", event.getPostId());

            PostDocument document = new PostDocument();
             document.setId(event.getPostId());
             document.setContent(event.getContent());
             document.setAuthorId(event.getAuthorId());
             document.setImageUrl(event.getImageUrl());
             document.setCreatedAt(event.getCreatedAt());


            postSearchRepository.save(document);

            log.info("Post indexed: {}", event.getPostId());
        } catch (Exception e) {

            log.error("Error indexing post: {}", event.getPostId());

        }

    }
}
