package com.sandeep.postservice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes", uniqueConstraints = @UniqueConstraint(
        columnNames = {"post_id","user_id"}
))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "post_id",nullable = false)
    private String postId;

    @Column(name = "user_id",nullable = false)
    private String userId;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
