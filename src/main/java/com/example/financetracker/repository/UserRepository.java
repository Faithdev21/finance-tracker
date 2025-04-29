package com.example.financetracker.repository;

import com.example.financetracker.entity.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    @Query("SELECT u FROM UserEntity u JOIN FETCH u.categories WHERE u.username = :username")
    Optional<UserEntity> findByUsernameWithCategories(String username);
}
