package com.sandeep.userservice.repository;

import com.sandeep.userservice.entity.Connection;
import com.sandeep.userservice.entity.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectionRepository extends JpaRepository<Connection,String> {

      boolean existsByRequesterIdAndReceiverId(String requesterId,String receiverId);
      List<Connection> findByRequesterIdAndStatus(String reuqesterId, ConnectionStatus status);

      List<Connection> findByReceiverIdAndStatus(String receiverId,ConnectionStatus status);
}
