package com.example.MyBookShopApp.repository;

import com.example.MyBookShopApp.logging.annotation.DebugLogs;
import com.example.MyBookShopApp.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@DebugLogs
public interface TagRepository extends JpaRepository<Tag, Integer> {

    Optional<Tag> findTagBySlug(String tagSlug);
}
