package com.example.FinanceTracker.repository;

import com.example.FinanceTracker.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByNameIn(List<String> name);
    boolean existsByName(String name);
    Optional<CategoryEntity> findByName(String name);


    @Query("SELECT c FROM CategoryEntity c JOIN c.users u WHERE u.id = :userId ORDER BY c.id")
    List<CategoryEntity> findCategoriesByUserId(@Param("userId") Long userId);

}
