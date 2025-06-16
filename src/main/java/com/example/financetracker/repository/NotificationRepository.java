package com.example.financetracker.repository;

import com.example.financetracker.entity.NotificationEntity;
import com.example.financetracker.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUserId (long userId);
    Optional<NotificationEntity> findTopByUserOrderByCreatedAtDesc(UserEntity user);

}
